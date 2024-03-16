
(ns monger.stages.adaptation
    (:import org.bson.types.ObjectId)
    (:require [fruits.bson.api            :as bson]
              [fruits.json.api            :as json]
              [monger.utils.api        :as monger.utils]
              [time.api :as time]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn adapt-document-id
  ; @note
  ; The source of document ID can be an (unreliable) user input, such as an address bar.
  ; Therefore, this function prevents printing error messages when the provided document ID is not a valid ObjectId string.
  ;
  ; @description
  ; Converts the given document ID string into an object.
  ;
  ; @param (string) document-id
  ;
  ; @usage
  ; (adapt-document-id "MyObjectId")
  ; =>
  ; #<ObjectId MyObjectId>
  ;
  ; @return (org.bson.types.ObjectId object)
  [document-id]
  (try (ObjectId. document-id)
       (catch Exception e nil)))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn adapt-document
  ; @description
  ; 1. Optionally parses the ID string into object within the given document.
  ; 2. Optionally renames the ':my-namespace/id' key to ':_id' (a MongoDB compatible identifier) within the given document.
  ; 3. Removes '.' characters from keys within the given document to prevent them misread as dot notations (BSON syntax requirement).
  ; 4. Converts keyword type keys and values into strings within the given document.
  ; 5. Parses date and time strings to objects within the given document.
  ; 6. Catches and re-throws errors, extending the error message with the provided state of the document.
  ;
  ; @param (map) document
  ; @param (map) options
  ; {:adapt-id? (boolean)(opt)
  ;  :parse-id? (boolean)(opt)}
  ;
  ; @usage
  ; (adapt-document {:my-namespace/id           "MyObjectId"
  ;                  :my-namespace/my-keyword   :my-value
  ;                  :my-namespace/my-string    "My value"
  ;                  :my-namespace/my-timestamp "2020-04-20T16:20:00.000Z"}
  ;                 {:adapt-id? true :parse-id? true})
  ; =>
  ; {"_id"                       #<ObjectId MyObjectId>
  ;  "my-namespace/my-keyword"   "*:my-value"
  ;  "my-namespace/my-string"    "My value"
  ;  "my-namespace/my-timestamp" #<DateTime 2020-04-20T16:20:00.123Z>}
  ;
  ; @return (map)
  [document {:keys [adapt-id? parse-id?]}]
  (try (cond-> document parse-id?            monger.utils/parse-id
                        adapt-id?            monger.utils/adapt-id
                        :undot-keys          bson/undot-keys
                        :unkeywordize-keys   json/unkeywordize-keys
                        :unkeywordize-values json/unkeywordize-values
                        :parse-timestamps    time/parse-timestamps)
       (catch Exception e (throw (ex-info e {:document document})))))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn adapt-query
  ; @description
  ; 1. Optionally parses ID strings into objects within the given query.
  ; 2. Optionally renames ':my-namespace/id' keys to ':_id' (a MongoDB compatible identifier) within the given query.
  ; 3. Converts keyword type keys and values into strings within the given query.
  ; 4. Parses date and time strings into objects within the given query.
  ; 5. Catches and re-throws errors, extending the error message with the provided state of the query.
  ;
  ; @param (map) query
  ; @param (map) options
  ; {:adapt-id? (boolean)(opt)
  ;  :parse-id? (boolean)(opt)}
  ;
  ; @usage
  ; (adapt-query {:my-namespace/id           "MyObjectId"
  ;               :my-namespace/my-keyword   :my-value
  ;               :my-namespace/my-string    "My value"
  ;               :my-namespace/my-timestamp "2020-04-20T16:20:00.000Z"
  ;               :$or [{:my-namespace/id "AnotherObjectId"}]}
  ;              {:adapt-id? true :parse-id? true})
  ; =>
  ; {"_id"                       #<ObjectId MyObjectId>
  ;  "my-namespace/my-keyword"   "*:my-value"
  ;  "my-namespace/my-string"    "My value"
  ;  "my-namespace/my-timestamp" #<DateTime 2020-04-20T16:20:00.123Z>
  ;  "$or" [{"my-namespace/id" #<ObjectId AnotherObjectId>}]}
  ;
  ; @return (map)
  [query {:keys [adapt-id? parse-id?]}]
  (try (if (->     query empty?)
           (->     query) ; A query can be an empty map.
           (cond-> query parse-id? (monger.utils/walk monger.utils/parse-id)
                         adapt-id? (monger.utils/walk monger.utils/adapt-id)
                         :unkeywordize-keys   json/unkeywordize-keys
                         :unkeywordize-values json/unkeywordize-values
                         :parse-timestamps    time/parse-timestamps))
       (catch Exception e (throw (ex-info e {:query query})))))

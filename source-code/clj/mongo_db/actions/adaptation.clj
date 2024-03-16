
(ns mongo-db.actions.adaptation
    (:import org.bson.types.ObjectId)
    (:require [fruits.bson.api            :as bson]
              [fruits.json.api            :as json]
              [mongo-db.core.utils        :as core.utils]
              [time.api                   :as time]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn document-id-input
  ; @ignore
  ;
  ; @description
  ; Converts the given document ID string into an object.
  ;
  ; @param (string) document-id
  ;
  ; @usage
  ; (document-id-input "MyObjectId")
  ; =>
  ; #<ObjectId MyObjectId>
  ;
  ; @return (org.bson.types.ObjectId object)
  [document-id]
  ; Prevents printing error messages when the document ID is not a valid ObjectId string.
  ; The source of document ID can be an (unreliable) user input, such as an address bar.
  (try (ObjectId. document-id)
       (catch Exception e nil)))

(defn document-id-output
  ; @ignore
  ;
  ; @description
  ; Converts the given document ID object into a string.
  ;
  ; @param (org.bson.types.ObjectId object) document-id
  ;
  ; @usage
  ; (document-id-output #<ObjectId MyObjectId>)
  ; =>
  ; "MyObjectId"
  ;
  ; @return (string)
  [document-id]
  (if document-id (str document-id)))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn document-input
  ; @ignore
  ;
  ; @description
  ; 1. Optionally parses the ID string into object within the given document.
  ; 2. Optionally renames the ':namespace/id' key to ':_id' (a MongoDB compatible identifier) within the given document.
  ; 3. Removes '.' characters from keys within the given document to prevent them misread as dot notations (BSON syntax requirement).
  ; 4. Converts keyword type keys and values into strings within the given document.
  ; 5. Parses date and time strings to objects within the given document.
  ; 6. Catches and re-throws errors, extending the error message with the ACTUAL state of the document.
  ;
  ; @param (namespaced map) document
  ; @param (map) options
  ; {:parse-id? (boolean)(opt)
  ;  :rename-id? (boolean)(opt)}
  ;
  ; @usage
  ; (document-input {:namespace/id           "MyObjectId"
  ;                  :namespace/my-keyword   :my-value
  ;                  :namespace/my-string    "My value"
  ;                  :namespace/my-timestamp "2020-04-20T16:20:00.000Z"}
  ;                 {:parse-id? true :rename-id? true})
  ; =>
  ; {"_id"                    #<ObjectId MyObjectId>
  ;  "namespace/my-keyword"   "*:my-value"
  ;  "namespace/my-string"    "My value"
  ;  "namespace/my-timestamp" #<DateTime 2020-04-20T16:20:00.123Z>}
  ;
  ; @return (namespaced map)
  [document {:keys [parse-id? rename-id?]}]
  (try (cond-> document parse-id?            core.utils/parse-id
                        rename-id?           core.utils/id->_id
                        :undot-keys          bson/undot-keys
                        :unkeywordize-keys   json/unkeywordize-keys
                        :unkeywordize-values json/unkeywordize-values
                        :parse-timestamps    time/parse-timestamps)
       (catch Exception e (throw (ex-info e {:document document})))))

(defn document-output
  ; @ignore
  ;
  ; @description
  ; 1. Converts string type keys and values into keywords within the given document (if they were keywords originally).
  ; 2. Unparses date and time objects into strings within the given document.
  ; 3. Optionally renames the ':_id' key to ':namespace/id' within the given document.
  ; 4. Optionally unparses the ID object into string within the given document.
  ; 5. Catches and re-throws errors, extending the error message with the ACTUAL state of the document.
  ;
  ; @param (namespaced map) document
  ; @param (map) options
  ; {:rename-id? (boolean)(opt)
  ;  :unparse-id? (boolean)(opt)}
  ;
  ; @example
  ; (document-output {"_id"                    #<ObjectId MyObjectId>
  ;                   "namespace/my-keyword"   "*:my-value"
  ;                   "namespace/my-string"    "My value"
  ;                   "namespace/my-timestamp" #<DateTime 2020-04-20T16:20:00.123Z>}
  ;                  {:rename-id? true :unparse-id? true})
  ; =>
  ; {:namespace/id           "MyObjectId"
  ;  :namespace/my-keyword   :my-value
  ;  :namespace/my-string    "My value"
  ;  :namespace/my-timestamp "2020-04-20T16:20:00.000Z"}
  ;
  ; @return (namespaced map)
  [document {:keys [rename-id? unparse-id?]}]
  (try (cond-> document :keywordize-keys    json/keywordize-keys
                        :keywordize-values  json/keywordize-values
                        :unparse-timestamps time/unparse-timestamps
                        rename-id?          core.utils/_id->id
                        unparse-id?         core.utils/unparse-id)
       (catch Exception e (throw (ex-info e {:document document})))))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn query-input
  ; @ignore
  ;
  ; @description
  ; 1. Optionally parses ID strings into objects within the given query.
  ; 2. Optionally renames ':namespace/id' keys to ':_id' (a MongoDB compatible identifier) within the given query.
  ; 3. Converts keyword type keys and values into strings within the given query.
  ; 4. Parses date and time strings into objects within the given query.
  ; 5. Catches and re-throws errors, extending the error message with the ACTUAL state of the query.
  ;
  ; @param (map) query
  ; @param (map) options
  ; {:parse-id? (boolean)(opt)
  ;  :rename-id? (boolean)(opt)}
  ;
  ; @usage
  ; (query-input {:namespace/id           "MyObjectId"
  ;               :namespace/my-keyword   :my-value
  ;               :namespace/my-string    "My value"
  ;               :namespace/my-timestamp "2020-04-20T16:20:00.000Z"
  ;               :$or [{:namespace/id "AnotherObjectId"}]}
  ;              {:parse-id? true :rename-id? true})
  ; =>
  ; {"_id"                    #<ObjectId MyObjectId>
  ;  "namespace/my-keyword"   "*:my-value"
  ;  "namespace/my-string"    "My value"
  ;  "namespace/my-timestamp" #<DateTime 2020-04-20T16:20:00.123Z>
  ;  "$or" [{"namespace/id" #<ObjectId AnotherObjectId>}]}
  ;
  ; @return (map)
  [query {:keys [parse-id? rename-id?]}]
  (try (if (->     query empty?)
           (->     query) ; A query can be an empty map.
           (cond-> query parse-id?  (core.utils/walk core.utils/parse-id)
                         rename-id? (core.utils/walk core.utils/id->_id)
                         :unkeywordize-keys   json/unkeywordize-keys
                         :unkeywordize-values json/unkeywordize-values
                         :parse-timestamps    time/parse-timestamps))
       (catch Exception e (throw (ex-info e {:query query})))))

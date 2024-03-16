
(ns monger.stages.normalization
    (:require [fruits.bson.api            :as bson]
              [fruits.json.api            :as json]
              [monger.utils.api        :as monger.utils]
              [time.api                   :as time]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn normalize-document-id
  ; @description
  ; Converts the given document ID object into a string.
  ;
  ; @param (org.bson.types.ObjectId object) document-id
  ;
  ; @usage
  ; (normalize-document-id #<ObjectId MyObjectId>)
  ; =>
  ; "MyObjectId"
  ;
  ; @return (string)
  [document-id]
  (if document-id (str document-id)))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn normalize-document
  ; @description
  ; 1. Converts string type keys and values into keywords within the given document (if they were converted from keywords).
  ; 2. Unparses date and time objects into strings within the given document.
  ; 3. Optionally renames the ':_id' key to ':my-namespace/id' within the given document.
  ; 4. Optionally unparses the ID object into string within the given document.
  ; 5. Catches and re-throws errors, extending the error message with the provided state of the document.
  ;
  ; @param (map) document
  ; @param (map) options
  ; {:normalize-id? (boolean)(opt)
  ;  :unparse-id? (boolean)(opt)}
  ;
  ; @example
  ; (normalize-document {"_id"                       #<ObjectId MyObjectId>
  ;                      "my-namespace/my-keyword"   "*:my-value"
  ;                      "my-namespace/my-string"    "My value"
  ;                      "my-namespace/my-timestamp" #<DateTime 2020-04-20T16:20:00.123Z>}
  ;                     {:normalize-id? true :unparse-id? true})
  ; =>
  ; {:my-namespace/id           "MyObjectId"
  ;  :my-namespace/my-keyword   :my-value
  ;  :my-namespace/my-string    "My value"
  ;  :my-namespace/my-timestamp "2020-04-20T16:20:00.000Z"}
  ;
  ; @return (map)
  [document {:keys [normalize-id? unparse-id?]}]
  (try (cond-> document :keywordize-keys    json/keywordize-keys
                        :keywordize-values  json/keywordize-values
                        :unparse-timestamps time/unparse-timestamps
                        normalize-id?       monger.utils/normalize-id
                        unparse-id?         monger.utils/unparse-id)
       (catch Exception e (throw (ex-info e {:document document})))))

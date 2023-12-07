
(ns mongo-db.actions.adaptation
    (:import org.bson.types.ObjectId)
    (:require [fruits.bson.api            :as bson]
              [fruits.json.api            :as json]
              [mongo-db.core.utils        :as core.utils]
              [mongo-db.reader.adaptation :as reader.adaptation]
              [time.api                   :as time]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn document-id-input
  ; @ignore
  ;
  ; @param (string) document-id
  ;
  ; @example
  ; (document-id-input "MyObjectId")
  ; =>
  ; #<ObjectId MyObjectId>
  ;
  ; @return (org.bson.types.ObjectId object)
  [document-id]
  (reader.adaptation/document-id-input document-id))

(defn document-id-output
  ; @ignore
  ;
  ; @param (org.bson.types.ObjectId object) document-id
  ;
  ; @example
  ; (document-id-output #<ObjectId MyObjectId>)
  ; =>
  ; "MyObjectId"
  ;
  ; @return (string)
  [document-id]
  (if document-id (str document-id)))

;; -- Inserting document ------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn insert-input
  ; @ignore
  ;
  ; @param (namespaced map) document
  ;
  ; @example
  ; (insert-input {:namespace/id            "MyObjectId"
  ;                :namespace/my-keyword    :my-value
  ;                :namespace/your-string   "your-value"
  ;                :namespace/our-timestamp "2020-04-20T16:20:00.000Z"})
  ; =>
  ; {"_id"                     #<ObjectId MyObjectId>
  ;  "namespace/my-keyword"    "*:my-value"
  ;  "namespace/your-string"   "your-value"
  ;  "namespace/our-timestamp" #<DateTime 2020-04-20T16:20:00.123Z>}
  ;
  ; @return (namespaced map)
  [document]
  ; 1. Renames the ':namespace/id' key to ':_id' key (a MongoDB compatible identifier) within the document.
  ;    Parses the identifier to ObjectId object.
  ; 2. Removes the '.' characters from the keys within the document to prevent them misread as dot notations (BSON syntax requirement).
  ; 3. Converts the keyword type keys and values to strings within the document.
  ; 4. Parses the date and time strings within the document to object types.
  (try (-> document (core.utils/id->_id {:parse? true}) bson/undot-keys json/unkeywordize-keys json/unkeywordize-values time/parse-timestamps)
       (catch Exception e (println (str e "\n" {:document document})))))

(defn insert-output
  ; @ignore
  ;
  ; @param (namespaced map) document
  ;
  ; @example
  ; (insert-output {"_id"                     #<ObjectId MyObjectId>
  ;                 "namespace/my-keyword"    "*:my-value"
  ;                 "namespace/your-string"   "your-value"
  ;                 "namespace/our-timestamp" #<DateTime 2020-04-20T16:20:00.123Z>})
  ; =>
  ; {:namespace/id            "MyObjectId"
  ;  :namespace/my-keyword    :my-value
  ;  :namespace/your-string   "your-value"
  ;  :namespace/our-timestamp "2020-04-20T16:20:00.000Z"}
  ;
  ; @return (namespaced map)
  [document]
  ; 1. Converts the keys and values back to keywords that was converted from keywords to strings (when it was stored).
  ; 2. Unparses the date and time objects within the document to string types.
  ; 3. Renames the ':_id' key (a MongoDB compatible identifier) to ':namespace/id' key within the document.
  ;    Unparses the identifier to string type.
  (try (-> document json/keywordize-keys json/keywordize-values time/unparse-timestamps (core.utils/_id->id {:unparse? true}))
       (catch Exception e (println (str e "\n" {:document document})))))

;; -- Saving document ---------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn save-input
  ; @ignore
  ;
  ; @param (namespaced map) document
  ;
  ; @example
  ; (save-input {:namespace/id            "MyObjectId"
  ;              :namespace/my-keyword    :my-value
  ;              :namespace/your-string   "your-value"
  ;              :namespace/our-timestamp "2020-04-20T16:20:00.000Z"})
  ; =>
  ; {"_id"                     #<ObjectId MyObjectId>
  ;  "namespace/my-keyword"    "*:my-value"
  ;  "namespace/your-string"   "your-value"
  ;  "namespace/our-timestamp" #<DateTime 2020-04-20T16:20:00.123Z>}
  ;
  ; @return (namespaced map)
  [document]
  ; 1. Renames the ':namespace/id' key to ':_id' key (a MongoDB compatible identifier) within the document.
  ;    Parses the identifier to ObjectId object.
  ; 2. Removes the '.' characters from the keys within the document to prevent them misread as dot notations (BSON syntax requirement).
  ; 3. Converts the keyword type keys and values to strings within the document.
  ; 4. Parses the date and time strings within the document to object types.
  (try (-> document (core.utils/id->_id {:parse? true}) bson/undot-keys json/unkeywordize-keys json/unkeywordize-values time/parse-timestamps)
       (catch Exception e (println (str e "\n" {:document document})))))

(defn save-output
  ; @ignore
  ;
  ; @param (namespaced map) document
  ;
  ; @example
  ; (save-output {"_id"                     #<ObjectId MyObjectId>
  ;               "namespace/my-keyword"    "*:my-value"
  ;               "namespace/your-string"   "your-value"
  ;               "namespace/our-timestamp" #<DateTime 2020-04-20T16:20:00.123Z>})
  ; =>
  ; {:namespace/id            "MyObjectId"
  ;  :namespace/my-keyword    :my-value
  ;  :namespace/your-string   "your-value"
  ;  :namespace/our-timestamp "2020-04-20T16:20:00.000Z"}
  ;
  ; @return (namespaced map)
  [document]
  ; 1. Converts the keys and values back to keywords that was converted from keywords to strings (when it was stored).
  ; 2. Unparses the date and time objects within the document to string types.
  ; 3. Renames the ':_id' key (a MongoDB compatible identifier) to ':namespace/id' key within the document.
  ;    Unparses the identifier to string type.
  (try (-> document json/keywordize-keys json/keywordize-values time/unparse-timestamps (core.utils/_id->id {:unparse? true}))
       (catch Exception e (println (str e "\n" {:document document})))))

;; -- Updating document -------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn update-input
  ; @ignore
  ;
  ; @param (namespaced map) document
  ;
  ; @example
  ; (update-input {:namespace/my-keyword    :my-value
  ;                :namespace/your-string   "your-value"
  ;                :namespace/our-timestamp "2020-04-20T16:20:00.000Z"})
  ; =>
  ; {"namespace/my-keyword"    "*:my-value"
  ;  "namespace/your-string"   "your-value"
  ;  "namespace/our-timestamp" #<DateTime 2020-04-20T16:20:00.123Z>}
  ;
  ; @return (namespaced map)
  [document]
  ; 1. Removes the '.' characters from the keys within the document to prevent them misread as dot notations (BSON syntax requirement).
  ; 2. Converts the keyword type keys and values to strings within the document.
  ; 4. Parses the date and time strings within the document to object types.
  (try (-> document bson/undot-keys json/unkeywordize-keys json/unkeywordize-values time/parse-timestamps)
       (catch Exception e (println (str e "\n" {:document document})))))

(defn update-query
  ; @ignore
  ;
  ; @param (map) query
  ;
  ; @return (map)
  [query]
  (reader.adaptation/find-query query))

;; -- Upserting document ------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn upsert-input
  ; @ignore
  ;
  ; @param (namespaced map) document
  ;
  ; @example
  ; (upsert-input {:namespace/my-keyword    :my-value
  ;                :namespace/your-string   "your-value"
  ;                :namespace/our-timestamp "2020-04-20T16:20:00.000Z"})
  ; =>
  ; {"namespace/my-keyword"    "*:my-value"
  ;  "namespace/your-string"   "your-value"
  ;  "namespace/our-timestamp" #<DateTime 2020-04-20T16:20:00.123Z>}
  ;
  ; @return (namespaced map)
  [document]
  (update-input document))

;; -- Duplicating document ----------------------------------------------------
;; ----------------------------------------------------------------------------

(defn duplicate-input
  ; @ignore
  ;
  ; @param (namespaced map) document
  ;
  ; @example
  ; (duplicate-input {:namespace/id            "MyObjectId"
  ;                   :namespace/my-keyword    :my-value
  ;                   :namespace/your-string   "your-value"
  ;                   :namespace/our-timestamp "2020-04-20T16:20:00.000Z"})
  ; =>
  ; {"_id"                     #<ObjectId MyObjectId>
  ;  "namespace/my-keyword"    "*:my-value"
  ;  "namespace/your-string"   "your-value"
  ;  "namespace/our-timestamp" #<DateTime 2020-04-20T16:20:00.123Z>}
  ;
  ; @return (namespaced map)
  [document]
  ; 1. Renames the ':namespace/id' key to ':_id' key (a MongoDB compatible identifier) within the document.
  ;    Parses the identifier to ObjectId object.
  ; 2. Removes the '.' characters from the keys within the document to prevent them misread as dot notations (BSON syntax requirement).
  ; 3. Converts the keyword type keys and values to strings within the document.
  ; 4. Parses the date and time strings within the document to object types.
  (try (-> document (core.utils/id->_id {:parse? true}) bson/undot-keys json/unkeywordize-keys json/unkeywordize-values time/parse-timestamps)
       (catch Exception e (println (str e "\n" {:document document})))))

(defn duplicate-output
  ; @ignore
  ;
  ; @param (namespaced map) document
  ;
  ; @example
  ; (insert-output {"_id"                     #<ObjectId MyObjectId>
  ;                 "namespace/my-keyword"    "*:my-value"
  ;                 "namespace/your-string"   "your-value"
  ;                 "namespace/our-timestamp" #<DateTime 2020-04-20T16:20:00.123Z>})
  ; =>
  ; {:namespace/id            "MyObjectId"
  ;  :namespace/my-keyword    :my-value
  ;  :namespace/your-string   "your-value"
  ;  :namespace/our-timestamp "2020-04-20T16:20:00.000Z"}
  ;
  ; @return (namespaced map)
  [document]
  ; 1. Converts the keys and values back to keywords that was converted from keywords to strings (when it was stored).
  ; 2. Unparses the date and time objects within the document to string types.
  ; 3. Renames the ':_id' key (a MongoDB compatible identifier) to ':namespace/id' key within the document.
  ;    Unparses the identifier to string type.
  (try (-> document json/keywordize-keys json/keywordize-values time/unparse-timestamps (core.utils/_id->id {:unparse? true}))
       (catch Exception e (println (str e "\n" {:document document})))))

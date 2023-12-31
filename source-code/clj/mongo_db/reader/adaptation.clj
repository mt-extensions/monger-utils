
(ns mongo-db.reader.adaptation
    (:import org.bson.types.ObjectId)
    (:require [fruits.json.api     :as json]
              [fruits.map.api      :as map]
              [mongo-db.core.utils :as core.utils]
              [time.api            :as time]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn document-id-input
  ; @ignore
  ;
  ; @description
  ; - Designed to handle cases where the document ID may come from user input,
  ;   and if the input is not a valid ObjectId, it doesn't raise an exception but instead returns NIL.
  ; - In some cases, the source of a document ID can be a URL entered into the browser's address bar.
  ;   This URL might contain values that cannot be converted into valid ObjectId objects.
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
  (try (ObjectId. document-id)
       ; It prevents (unnecessary) error messages being printed,
       ; in cases when the document ID provided as not a valid ObjectId.
       ; (catch Exception e (println (str e "\n" {:document-id document-id})))
       (catch Exception e nil)))

;; -- Find document -----------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn find-output
  ; @ignore
  ;
  ; @param (namespaced map) document
  ;
  ; @return (namespaced map)
  [document]
  ; 1. Converts values back to keywords that was converted from keywords to strings (when it was stored).
  ; 2. Unparses the date and time objects within the document to string types.
  ; 3. Renames the ':_id' key (a MongoDB compatible identifier) to ':namespace/id' key within the document.
  ;    Unparses the identifier to string type.
  (try (-> document json/keywordize-values time/unparse-timestamps (core.utils/_id->id {:unparse? true}))
       (catch Exception e (println (str e "\n" {:document document})))))

(defn find-query
  ; @ignore
  ;
  ; @param (map) query
  ;
  ; @example
  ; (find-query {:namespace/id            "MyObjectId"
  ;              :namespace/my-keyword    :my-value
  ;              :namespace/my-string     "My value"
  ;              :namespace/our-timestamp "2020-04-20T16:20:00.000Z"
  ;              :$or [{:namespace/id "AnotherObjectId"}]})
  ; =>
  ; {"_id"                     #<ObjectId MyObjectId>
  ;  "namespace/my-keyword"    "*:my-value"
  ;  "namespace/my-string"     "My value"
  ;  "namespace/our-timestamp" #<DateTime 2020-04-20T16:20:00.123Z>
  ;  "$or" [{"namespace/id" #<ObjectId AnotherObjectId>}]}
  ;
  ; @return (map)
  [query]
  (if (map/nonempty? query)
      ; 1. Renames the ':namespace/id' key to ':_id' key (a MongoDB compatible identifier) within the query.
      ;    Parses all the identifiers within the query to ObjectId objects.
      ; 2. Converts the keyword type keys and values to strings within the document.
      ; 4. Parses the date and time strings within the document to object types.
      (try (-> query (core.utils/id->>_id {:parse? true}) json/unkeywordize-keys json/unkeywordize-values time/parse-timestamps)
           (catch Exception e (println (str e "\n" {:query query}))))

      ; The given 'query' value could be an empty map. In this case it returned as-is.
      (-> query)))

(defn find-projection
  ; @ignore
  ;
  ; @param (namespaced map) projection
  ;
  ; @example
  ; (find-projection {:namespace/my-key 0})
  ; =>
  ; {"namespace/my-key" 0}
  ;
  ; @example
  ; (find-projection {:namespace/id     0
  ;                   :namespace/my-key 0})
  ; =>
  ; {"_id"              0
  ;  "namespace/my-key" 0}
  ;
  ; @example
  ; (find-projection nil)
  ; =>
  ; {}
  ;
  ; @return (namespaced map)
  [projection]
  ; Functions in the 'mongo-db.reader.engine' namespace always apply the 'find-projection' function
  ; even if the given 'projection' value is NIL. Therefore, the '(if projection ...)' condition is necessary.
  (if projection (try (-> projection (core.utils/id->_id {:parse? false}) json/unkeywordize-keys)
                      (catch Exception e (println (str e "\n" {:projection projection}))))
                 (-> {})))


(ns mongo-db.reader.adaptation
    (:import org.bson.types.ObjectId)
    (:require [json.api            :as json]
              [map.api             :as map]
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
  ; 1. A dokumentumban használt string típusra alakított értékek átalakítása kulcsszó típusra
  ; 2. A dokumentumban objektum típusként tárolt dátumok és idők átalakítása string típusra
  ; 3. A dokumentum :_id tulajdonságának átnevezése :namespace/id tulajdonságra
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
  ;              :namespace/your-string   "your-value"
  ;              :namespace/our-timestamp "2020-04-20T16:20:00.000Z"
  ;              :$or [{:namespace/id "YourObjectId"}]})
  ; =>
  ; {"_id"                     #<ObjectId MyObjectId>
  ;  "namespace/my-keyword"    "*:my-value"
  ;  "namespace/your-string"   "your-value"
  ;  "namespace/our-timestamp" #<DateTime 2020-04-20T16:20:00.123Z>
  ;  "$or" [{"namespace/id" #<ObjectId YourObjectId>}]}
  ;
  ; @return (map)
  [query]
  (if (map/nonempty? query)
      ; 1. A query térképben található :namespace/id tulajdonságok átnevezése :_id tulajdonságra
      ;    A query térképben található string típusú azonosítók átalakítása objektum típusra
      ; 2. A query térképben használt kulcsszó típusú kulcsok és értékek átalakítása string típusra
      ; 3. A query térképben string típusként tárolt dátumok és idők átalakítása objektum típusra
      (try (-> query (core.utils/id->>_id {:parse? true}) json/unkeywordize-keys json/unkeywordize-values time/parse-timestamps)
           (catch Exception e (println (str e "\n" {:query query}))))

      ; The 'query' could be an empty map. In this case it returned as-is.
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
  ; The functions in the 'mongo-db.reader' namespace apply the 'find-projection' function
  ; even if the projection value is nil. Therefore, it is necessary to use the '(if projection ...)'
  ; condition.
  (if projection (try (-> projection (core.utils/id->_id {:parse? false}) json/unkeywordize-keys)
                      (catch Exception e (println (str e "\n" {:projection projection}))))
                 (-> {})))

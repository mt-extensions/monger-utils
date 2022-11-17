
(ns mongo-db.adaptation
    (:import  org.bson.types.ObjectId)
    (:require [candy.api        :refer [return]]
              [json.api         :as json]
              [map.api          :as map]
              [mongo-db.engine  :as engine]
              [mongo-db.errors  :as errors]
              [time.api         :as time]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn document-id-input
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
      ; A document-id azonosító forrása egyes esetekben a böngésző címsorába írt url lehet,
      ; ami lehetővé teszi, hogy ObjectId objektummá nem alakítható érték is átadódhat
      ; a document-id-input függvénynek, ami feleslegesen jelenítene meg hibaüzeneteket.
      ;(catch Exception e (println (str e "\n" {:document-id document-id})))
       (catch Exception e nil)))

(defn document-id-output
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

;; -- Aggregating documents ---------------------------------------------------
;; ----------------------------------------------------------------------------

(defn aggregation-output
  ; @param (DBObject) n
  ;
  ; @return (maps in vector)
  [n]
  (try (-> n engine/DBObject->edn (get-in [:cursor :firstBatch]))
       (catch Exception e (println (str e "\n" {:aggregation-output n})))))

;; -- Find document -----------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn find-output
  ; @param (namespaced map) document
  ;
  ; @return (namespaced map)
  [document]
  ; 1. A dokumentumban használt string típusra alakított értékek átalakítása kulcsszó típusra
  ; 2. A dokumentumban objektum típusként tárolt dátumok és idők átalakítása string típusra
  ; 3. A dokumentum :_id tulajdonságának átnevezése :namespace/id tulajdonságra
  (try (-> document json/keywordize-values time/unparse-date-time (engine/_id->id {:unparse? true}))
       (catch Exception e (println (str e "\n" {:document document})))))

(defn find-query
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
      ;   A query térképben található string típusú azonosítók átalakítása objektum típusra
      ; 2. A query térképben használt kulcsszó típusú kulcsok és értékek átalakítása string típusra
      ; 3. A query térképben string típusként tárolt dátumok és idők átalakítása objektum típusra
      (try (-> query engine/id->>_id json/unkeywordize-keys json/unkeywordize-values time/parse-date-time)
           (catch Exception e (println (str e "\n" {:query query}))))
      ; A query térképként lehetséges üres térképet is átadni.
      (return {})))

(defn find-projection
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
  ; @return (namespaced map)
  [projection]
  (try (-> projection (engine/id->_id {:parse? false}) json/unkeywordize-keys)
       (catch Exception e (println (str e "\n" {:projection projection})))))

;; -- Inserting document ------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn insert-input
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
  ; 1. A dokumentum :namespace/id tulajdonságának átnevezése :_id tulajdonságra
  ;   A dokumentum string típusú azonosítójának átalakítása objektum típusra
  ; 2. A dokumentumban használt kulcsszó típusú kulcsok és értékek átalakítása string típusra
  ; 3. A dokumentumban string típusként tárolt dátumok és idők átalakítása objektum típusra
  (try (-> document (engine/id->_id {:parse? true}) json/unkeywordize-keys json/unkeywordize-values time/parse-date-time)
       (catch Exception e (println (str e "\n" {:document document})))))

(defn insert-output
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
  ; 1. A dokumentumban használt string típusra alakított kulcsok és értékek átalakítása kulcsszó típusra
  ; 2. A dokumentumban objektum típusként tárolt dátumok és idők átalakítása string típusra
  ; 3. A dokumentum :_id tulajdonságának átnevezése :namespace/id tulajdonságra
  ;   A dokumentum objektum típusú azonosítójának átalakítása string típusra
  (try (-> document json/keywordize-keys json/keywordize-values time/unparse-date-time (engine/_id->id {:unparse? true}))
       (catch Exception e (println (str e "\n" {:document document})))))

;; -- Saving document ---------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn save-input
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
  ; 1. A dokumentum :namespace/id tulajdonságának átnevezése :_id tulajdonságra
  ;   A dokumentum string típusú azonosítójának átalakítása objektum típusra
  ; 2. A dokumentumban használt kulcsszó típusú kulcsok és értékek átalakítása string típusra
  ; 3. A dokumentumban string típusként tárolt dátumok és idők átalakítása objektum típusra
  (try (-> document (engine/id->_id {:parse? true}) json/unkeywordize-keys json/unkeywordize-values time/parse-date-time)
       (catch Exception e (println (str e "\n" {:document document})))))

(defn save-output
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
  ; 1. A dokumentumban használt string típusra alakított kulcsok és értékek átalakítása kulcsszó típusra
  ; 2. A dokumentumban objektum típusként tárolt dátumok és idők átalakítása string típusra
  ; 3. A dokumentum :_id tulajdonságának átnevezése :namespace/id tulajdonságra
  ;   A dokumentum objektum típusú azonosítójának átalakítása string típusra
  (try (-> document json/keywordize-keys json/keywordize-values time/unparse-date-time (engine/_id->id {:unparse? true}))
       (catch Exception e (println (str e "\n" {:document document})))))

;; -- Updating document -------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn update-input
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
  ; 1. A dokumentumban használt kulcsszó típusú kulcsok és értékek átalakítása string típusra
  ; 2. A dokumentumban string típusként tárolt dátumok és idők átalakítása objektum típusra
  (try (-> document json/unkeywordize-keys json/unkeywordize-values time/parse-date-time)
       (catch Exception e (println (str e "\n" {:document document})))))

(defn update-query
  ; @param (map) query
  ;
  ; @return (map)
  [query]
  (find-query query))

;; -- Upserting document ------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn upsert-input
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
  ; 1. A dokumentum :namespace/id tulajdonságának átnevezése :_id tulajdonságra
  ;   A dokumentum string típusú azonosítójának átalakítása objektum típusra
  ; 2. A dokumentumban használt kulcsszó típusú kulcsok és értékek átalakítása string típusra
  ; 3. A dokumentumban string típusként tárolt dátumok és idők átalakítása objektum típusra
  (try (-> document (engine/id->_id {:parse? true}) json/unkeywordize-keys json/unkeywordize-values time/parse-date-time)
       (catch Exception e (println (str e "\n" {:document document})))))

(defn duplicate-output
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
  ; 1. A dokumentumban használt string típusra alakított kulcsok és értékek átalakítása kulcsszó típusra
  ; 2. A dokumentumban objektum típusként tárolt dátumok és idők átalakítása string típusra
  ; 3. A dokumentum :_id tulajdonságának átnevezése :namespace/id tulajdonságra
  ;   A dokumentum objektum típusú azonosítójának átalakítása string típusra
  (try (-> document json/keywordize-keys json/keywordize-values time/unparse-date-time (engine/_id->id {:unparse? true}))
       (catch Exception e (println (str e "\n" {:document document})))))

;; -- Aggregation -------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn search-query
  ; @param (namespaced map) query
  ;
  ; @example
  ; (search-query {:namespace/my-key "Xyz"}
  ; =>
  ; {"namespace/my-key" {"$regex" "Xyz" "$options" "i"}}
  ;
  ; @return (namespaced map)
  [query]
  (letfn [(adapt-value [v] {"$regex" v "$options" "i"})]
         (try (map/->kv query #(json/unkeywordize-key %)
                              #(adapt-value           %))
              (catch Exception e (println (str e "\n" {:query query}))))))

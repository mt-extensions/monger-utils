
(ns mongo-db.reader.adaptation
    (:import org.bson.types.ObjectId)
    (:require [json.api              :as json]
              [map.api               :as map]
              [mongo-db.core.helpers :as core.helpers]
              [noop.api              :refer [return]]
              [time.api              :as time]))

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
      ; a document-id-input függvénynek.
      ; A document-id-input függvény hibakiíratása azért van kikapcsolva, hogy
      ; a felhasználóktól érkező lekérésekben lévő esetlegesen hibás document-id azonosítók
      ; miatt ne jelenítsen meg feleslegesen hibaüzeneteket.
      ;(catch Exception e (println (str e "\n" {:document-id document-id})))
       (catch Exception e nil)))

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
  (try (-> document json/keywordize-values time/unparse-date-time (core.helpers/_id->id {:unparse? true}))
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
      ;    A query térképben található string típusú azonosítók átalakítása objektum típusra
      ; 2. A query térképben használt kulcsszó típusú kulcsok és értékek átalakítása string típusra
      ; 3. A query térképben string típusként tárolt dátumok és idők átalakítása objektum típusra
      (try (-> query (core.helpers/id->>_id {:parse? true}) json/unkeywordize-keys json/unkeywordize-values time/parse-date-time)
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
  ; @example
  ; (find-projection nil)
  ; =>
  ; {}
  ;
  ; @return (namespaced map)
  [projection]
  ; A mongo-db.reader névtér függvényei abban az esetben is alkalmazzák a find-projection
  ; függvényt, ha a projection értéke nil, emiatt szükségess az (if projection ...)
  ; függvény alkalmazása!
  (if projection (try (-> projection (core.helpers/id->_id {:parse? false}) json/unkeywordize-keys)
                      (catch Exception e (println (str e "\n" {:projection projection}))))
                 (return {})))


(ns mongo-db.aggregation.adaptation
    (:require [json.api                   :as json]
              [map.api                    :as map]
              [mongo-db.core.utils        :as core.utils]
              [mongo-db.reader.adaptation :as reader.adaptation]))

;; -- Aggregating documents ---------------------------------------------------
;; ----------------------------------------------------------------------------

(defn aggregation-output
  ; @ignore
  ;
  ; @param (DBObject) n
  ;
  ; @return (maps in vector)
  [n]
  (try (-> n core.utils/DBObject->edn (get-in [:cursor :firstBatch]))
       (catch Exception e (println (str e "\n" {:aggregation-output n})))))

(defn filter-query
  ; @ignore
  ;
  ; @param (map) query
  ; {:$or (maps in vector)(opt)
  ;  :$and (maps in vector)(opt)
  ;
  ; @example
  ; (filter-query {:namespace/my-keyword :my-value
  ;                :$or  [{:namespace/my-boolean   false}
  ;                       {:namespace/my-boolean   nil}]
  ;                :$and [{:namespace/your-boolean true}]})
  ; =>
  ; {"namespace/my-keyword" "*:my-value"
  ;  "$or"  [{"namespace/my-boolean"   false}
  ;          {"namespace/my-boolean"   nil}]
  ;  "$and" [{"namespace/your-boolean" true}]}
  ;
  ; @return (map)
  [query]
  (reader.adaptation/find-query query))

(defn search-query
  ; @ignore
  ;
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

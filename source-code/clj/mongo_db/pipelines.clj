
(ns mongo-db.pipelines
    (:require [mid-fruits.candy    :refer [param]]
              [mid-fruits.json     :as json]
              [mid-fruits.keyword  :as keyword]
              [mid-fruits.map      :as map]
              [mid-fruits.vector   :as vector]
              [mongo-db.adaptation :as adaptation]
              [mongo-db.checking   :as checking]))

;; -- Names -------------------------------------------------------------------
;; ----------------------------------------------------------------------------

; @name pipeline
;  An aggregation pipeline can return results for groups of documents.
;
; @name stage
;  Each stage performs an operation on the input documents.
;
; @name operation
;  TODO ...

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn add-fields-query
  ; @param (map) field-pattern
  ;
  ; @example
  ;  (add-fields-operation {:namespace/name  {:$concat [:$namespace/first-name " " :$namespace/last-name]}
  ;                         :namespace/total {:$sum     :$namespace/all-result}})
  ;  =>
  ;  {"namespace/name"  {"$concat" ["$namespace/first-name" " " "$namespace/last-name"]}
  ;   "namespace/total" {"$sum"     "$namespace/all-result"}}}
  ;
  ; @return (map)
  [field-pattern]
  ; A kulcsok és értékek függvénye is json/unkeywordize-key, mert szükségetlen a json/unkeywordize-value
  ; függvény által használt biztonsági prefixum alkalmazása.
  (map/->>kv field-pattern json/unkeywordize-key json/unkeywordize-key))

(defn filter-query
  ; @param (map) filter-pattern
  ;  {:$or (maps in vector)(opt)
  ;   :$and (maps in vector)(opt)
  ;
  ; @example
  ;  (filter-query {:namespace/my-keyword :my-value
  ;                 :$or  [{:namespace/my-boolean   false}
  ;                        {:namespace/my-boolean   nil}]
  ;                 :$and [{:namespace/your-boolean true}]})
  ;  =>
  ;  {"namespace/my-keyword" "*:my-value"
  ;   "$or"  [{"namespace/my-boolean"   false}
  ;           {"namespace/my-boolean"   nil}]
  ;   "$and" [{"namespace/your-boolean" true}]}
  ;
  ; @return (maps in vector)
  [filter-pattern]
  (adaptation/find-query filter-pattern))

(defn search-query
  ; @param (map) search-pattern
  ;  {:$and (maps in vector)(opt)
  ;   :$or (maps in vector)(opt)}
  ;
  ; @example
  ;  (search-query {:$or [{:namespace/my-string   "My value"}
  ;                       {:namespace/your-string "Your value"}]})
  ;  =>
  ;  {"$or" [{"namespace/my-string"   {"$regex" "My value" "$options" "i"}}
  ;          {"namespace/your-string" {"$regex" "Your value" "$options" "i"}}]}
  ;
  ; @return (map)
  ;  {"$and" (maps in vector)
  ;   "$or" (maps in vector)}
  [{:keys [$and $or]}]
  (cond-> {} $and (assoc "$and" (vector/->items $and #(-> % checking/search-query adaptation/search-query)))
             $or  (assoc "$or"  (vector/->items $or  #(-> % checking/search-query adaptation/search-query)))))

(defn sort-query
  ; @param (map) sort-pattern
  ;
  ; @example
  ;  (sort-query {:namespace/my-string -1 ...})
  ;  =>
  ;  {"namespace/my-string" -1 ...}
  ;
  ; @return (map)
  [sort-pattern]
  (map/->keys sort-pattern json/unkeywordize-key))

(defn unset-query
  ; @param (namespaced keywords in vector) unset-pattern
  ;
  ; @example
  ;  (unset-query [:namespace/my-string :namespace/your-string])
  ;  =>
  ;  ["namespace/my-string" "namespace/your-string"]
  ;
  ; @return (strings in vector)
  [unset-pattern]
  (vector/->items unset-pattern json/unkeywordize-key))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn get-pipeline
  ; @param (map) pipeline-props
  ;  {:field-pattern (map)(opt)
  ;   :filter-pattern (map)(opt)
  ;   :max-count (integer)(opt)
  ;   :search-pattern (map)(opt)
  ;   :skip (integer)(opt)
  ;   :sort-pattern (map)(opt)
  ;   :unset-pattern (namespaced keywords in vector)(opt)}
  ;
  ; @usage
  ;  (get-pipeline {:field-pattern  {:namespace/name {:$concat [:$namespace/first-name " " :$namespace/last-name]}
  ;                 :filter-pattern {:namespace/my-keyword :my-value
  ;                                  :$or [{:namespace/my-boolean   false}
  ;                                        {:namespace/my-boolean   nil}]}
  ;                 :search-pattern {:$or [{:namespace/my-string   "My value"}
  ;                                        {:namespace/your-string "Your value"}]}
  ;                 :sort-pattern   {:namespace/my-string -1}
  ;                 :unset-pattern  [:namespace/my-string :namespace/your-string]
  ;                 :max-count 20
  ;                 :skip      40})
  ;
  ; @return (maps in vector)
  [{:keys [field-pattern filter-pattern max-count search-pattern skip sort-pattern unset-pattern]}]
             ; Az $addFields operátor a $match és $sort operátorok végrehajtása előtt adja hozzá a virtuális mező(ke)t ...
  (cond-> [] field-pattern (conj {"$addFields"      (add-fields-query field-pattern)})
             :match        (conj {"$match" {"$and" [(filter-query     filter-pattern)
                                                    (search-query     search-pattern)]}})
             sort-pattern  (conj {"$sort"           (sort-query       sort-pattern)})
             ; Az $unset operátor a $match és $sort operátorok végrehajtása után távolítja el az eltávolítandó mező(ke)t ...
             unset-pattern (conj {"$unset"          (unset-query      unset-pattern)})
             skip          (conj {"$skip"           (param            skip)})
             max-count     (conj {"$limit"          (param            max-count)})))

(defn count-pipeline
  ; @param (map) pipeline-props
  ;  {:field-pattern (map)(opt)
  ;   :filter-pattern (map)(opt)
  ;   :search-pattern (map)(opt)}
  ;
  ; @usage
  ;  (count-pipeline {:field-pattern  {:namespace/name {:$concat [:$namespace/first-name " " :$namespace/last-name]}
  ;                   :filter-pattern {:namespace/my-keyword :my-value
  ;                                    :$or [{:namespace/my-boolean   false}
  ;                                          {:namespace/my-boolean   nil}]}
  ;                   :search-pattern {:$or [{:namespace/my-string   "My value"}]
  ;                                          {:namespace/your-string "Your value"}]}})
  ;
  ; @return (maps in vector)
  [{:keys [field-pattern filter-pattern search-pattern]}]
  (cond-> [] field-pattern (conj {"$addFields"      (add-fields-query field-pattern)})
             :match        (conj {"$match" {"$and" [(filter-query filter-pattern)
                                                    (search-query search-pattern)]}})))

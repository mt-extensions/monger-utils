
(ns mongo-db.aggregation.pipelines
    (:require [fruits.json.api                 :as json]
              [fruits.map.api                  :as map]
              [fruits.vector.api               :as vector]
              [mongo-db.aggregation.adaptation :as aggregation.adaptation]
              [mongo-db.aggregation.checking   :as aggregation.checking]
              [mongo-db.core.utils             :as core.utils]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

; @description
; Aggregation pipelines are a framework for performing data transformations using a series of processing stages.
; Aggregation pipelines allow you to process and transform data within the database itself.
; A pipeline consists of stages, each of which performs a specific operation on the data.

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn add-fields-query
  ; @ignore
  ;
  ; @param (map) field-pattern
  ;
  ; @example
  ; (add-fields-query {:namespace/name  {:$concat [:$namespace/first-name " " :$namespace/last-name]}
  ;                    :namespace/total {:$sum     :$namespace/all-result}})
  ; =>
  ; {"namespace/name"  {"$concat" ["$namespace/first-name" " " "$namespace/last-name"]}
  ;  "namespace/total" {"$sum"     "$namespace/all-result"}}}
  ;
  ; @return (map)
  [field-pattern]
  ; The 'add-fields-query' function instead of applying the 'json/unkeywordize-value'
  ; on values, it applies the 'json/unkeywordize-key' on both the keys and the values,
  ; because the 'json/unkeywordize-value' function would prepend a prefix to the values!
  (map/->>kv field-pattern json/unkeywordize-key json/unkeywordize-key))

(defn filter-query
  ; @ignore
  ;
  ; @param (map) filter-pattern
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
  [filter-pattern]
  (aggregation.adaptation/filter-query filter-pattern))

(defn search-query
  ; @ignore
  ;
  ; @param (map) search-pattern
  ; {:$and (maps in vector)(opt)
  ;  :$or (maps in vector)(opt)}
  ;
  ; @example
  ; (search-query {:$or [{:namespace/my-string   "My value"}
  ;                      {:namespace/your-string "Your value"}]})
  ; =>
  ; {"$or" [{"namespace/my-string"   {"$regex" "My value" "$options" "i"}}
  ;         {"namespace/your-string" {"$regex" "Your value" "$options" "i"}}]}
  ;
  ; @return (map)
  ; {"$and" (maps in vector)
  ;  "$or" (maps in vector)}
  [{:keys [$and $or]}]
  (cond-> {} $and (assoc "$and" (vector/->items $and #(-> % aggregation.checking/search-query aggregation.adaptation/search-query)))
             $or  (assoc "$or"  (vector/->items $or  #(-> % aggregation.checking/search-query aggregation.adaptation/search-query)))))

(defn sort-query
  ; @ignore
  ;
  ; @param (map) sort-pattern
  ;
  ; @example
  ; (sort-query {:namespace/my-string -1 ...})
  ; =>
  ; {"namespace/my-string" -1 ...}
  ;
  ; @return (map)
  [sort-pattern]
  ; BUG#8871
  ; https://jira.mongodb.org/browse/SERVER-51498
  ; Sorting on duplicate values causes repeated results with limit and skip.
  ;
  ; https://www.mongodb.com/docs/manual/reference/method/cursor.sort/#sort-cursor-stable-sorting
  ; To achieve a consistent sort, add a field which contains exclusively unique values to the sort.
  (-> sort-pattern (core.utils/id->_id)
                   (map/->keys json/unkeywordize-key)))

(defn unset-query
  ; @ignore
  ;
  ; @param (namespaced keywords in vector) unset-pattern
  ;
  ; @example
  ; (unset-query [:namespace/my-string :namespace/your-string])
  ; =>
  ; ["namespace/my-string" "namespace/your-string"]
  ;
  ; @return (strings in vector)
  [unset-pattern]
  (vector/->items unset-pattern json/unkeywordize-key))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn get-pipeline
  ; @param (map) pipeline-props
  ; {:field-pattern (map)(opt)
  ;  :filter-pattern (map)(opt)
  ;  :max-count (integer)(opt)
  ;  :search-pattern (map)(opt)
  ;  :skip (integer)(opt)
  ;  :sort-pattern (map)(opt)
  ;  :unset-pattern (namespaced keywords in vector)(opt)}
  ;
  ; @usage
  ; (get-pipeline {:field-pattern  {:namespace/name {:$concat [:$namespace/first-name " " :$namespace/last-name]}
  ;                :filter-pattern {:namespace/my-keyword :my-value
  ;                                 :$or [{:namespace/my-boolean  false}
  ;                                       {:namespace/my-boolean  nil}]}
  ;                :search-pattern {:$or [{:namespace/my-string   "My value"}
  ;                                       {:namespace/your-string "Your value"}]}
  ;                :sort-pattern   {:namespace/my-string -1}
  ;                :unset-pattern  [:namespace/my-string :namespace/your-string]
  ;                :max-count 20
  ;                :skip      40})
  ;
  ; @return (maps in vector)
  [{:keys [field-pattern filter-pattern max-count search-pattern skip sort-pattern unset-pattern]}]
  ; The '$addFields' operator - which adds virtual fields - must be placed before the '$match' and the '$sort' operators!
  ; The '$unset' operator - which removes virtual fields - must be placed after the '$match' and the '$sort' operators!
  (cond-> [] field-pattern (conj {"$addFields"     (add-fields-query field-pattern)})
             :match        (conj {"$match" {"$and" [(filter-query     filter-pattern)
                                                    (search-query     search-pattern)]}})
             sort-pattern  (conj {"$sort"           (sort-query       sort-pattern)})
             unset-pattern (conj {"$unset"          (unset-query      unset-pattern)})
             skip          (conj {"$skip"           (->               skip)})
             max-count     (conj {"$limit"          (->               max-count)})))

(defn count-pipeline
  ; @param (map) pipeline-props
  ; {:field-pattern (map)(opt)
  ;  :filter-pattern (map)(opt)
  ;  :search-pattern (map)(opt)}
  ;
  ; @usage
  ; (count-pipeline {:field-pattern  {:namespace/name {:$concat [:$namespace/first-name " " :$namespace/last-name]}
  ;                  :filter-pattern {:namespace/my-keyword :my-value
  ;                                   :$or [{:namespace/my-boolean   false}
  ;                                         {:namespace/my-boolean   nil}]}
  ;                  :search-pattern {:$or [{:namespace/my-string   "My value"}]
  ;                                         {:namespace/your-string "Your value"}]}})
  ;
  ; @return (maps in vector)
  [{:keys [field-pattern filter-pattern search-pattern]}]
  (cond-> [] field-pattern (conj {"$addFields"     (add-fields-query field-pattern)})
             :match        (conj {"$match" {"$and" [(filter-query filter-pattern)
                                                    (search-query search-pattern)]}})))

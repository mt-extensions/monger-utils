
(ns mongo-db.aggregation.checking
    (:require [mongo-db.reader.checking :as reader.checking]))

;; -- Aggregation -------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn search-query
  ; @ignore
  ;
  ; @param (*) query
  ;
  ; @return (*)
  [query]
  (reader.checking/find-query query))

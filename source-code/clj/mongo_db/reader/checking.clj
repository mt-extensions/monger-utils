
(ns mongo-db.reader.checking
    (:require [mongo-db.core.errors :as core.errors]
              [noop.api             :refer [return]]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn find-query
  ; @ignore
  ;
  ; @param (*) query
  ;
  ; @usage
  ; (find-query {:namespace/my-string "my-value"
  ;              :$or [{:namespace/id "MyObjectId"}]})
  ;
  ; @return (*)
  [query]
  (try (if (map?   query)
           (return query)
           (throw (Exception. core.errors/QUERY-MUST-BE-MAP-ERROR)))
       (catch Exception e (println (str e "\n" {:query query})))))

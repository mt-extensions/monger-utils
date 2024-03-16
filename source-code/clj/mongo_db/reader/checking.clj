
(ns mongo-db.reader.checking
    (:require [mongo-db.core.messages :as core.messages]))

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
  (try (if (-> query map?)
           (-> query)
           (throw (Exception. core.messages/QUERY-TYPE-ERROR)))
       (catch Exception e (println (str e "\n" {:query query})))))


(ns mongo-db.reader.prototyping
    (:require [noop.api :refer [return]]))

;; -- Find document -----------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn find-output
  ; @ignore
  ;
  ; @param (string) collection-path
  ; @param (namespaced map) document
  ; @param (map) options
  ; {:prototype-f (function)(opt)}
  ;
  ; @return (*)
  [collection-path document {:keys [prototype-f] :as options}]
  (try (if prototype-f (prototype-f document)
                       (return      document))
       (catch Exception e (println (str e "\n" {:collection-path collection-path :document document :options options})))))


(ns mongo-db.aggregation.helpers
    (:require [monger.core  :as mcr]
              [re-frame.api :as r]))

;; -- Error handling ----------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn command
  ; @param (map) options
  ;
  ; @return (DBObject)
  [options]
  (let [database @(r/subscribe [:mongo-db/get-connection])]
       (try (mcr/command database options)
            (catch Exception e (println (str e "\n" {:options options}))))))

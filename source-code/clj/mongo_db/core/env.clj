
(ns mongo-db.core.env
    (:require [monger.core               :as mcr]
              [mongo-db.connection.state :as connection.state]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn command
  ; @param (map) options
  ; {:warn? (boolean)(opt)
  ;   Default: true}
  ;
  ; @return (DBObject)
  [{:keys [warn?] :or {warn? true} :as options}]
  (try (mcr/command @connection.state/REFERENCE options)
       (catch Exception e (if warn? (println (str e "\n" {:options options}))))))

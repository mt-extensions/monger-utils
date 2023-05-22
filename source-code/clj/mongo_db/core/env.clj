
(ns mongo-db.core.env
    (:require [monger.core               :as mcr]
              [mongo-db.connection.state :as connection.state]
              [mongo-db.connection.utils :as connection.utils]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn command
  ; @param (string)(opt) database-name
  ; @param (map) options
  ; {:warn? (boolean)(opt)
  ;   Default: true}
  ;
  ; @return (DBObject)
  ([options]
   (let [database-name (connection.utils/default-database-name)]
        (command database-name options)))

  ([database-name {:keys [warn?] :or {warn? true} :as options}]
   (let [database-reference (get @connection.state/REFERENCES database-name)]
        (try (mcr/command database-reference options)
             (catch Exception e (if warn? (println (str e "\n" {:options options}))))))))

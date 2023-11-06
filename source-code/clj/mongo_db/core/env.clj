
(ns mongo-db.core.env
    (:require [monger.core               :as mcr]
              [mongo-db.connection.state :as connection.state]
              [mongo-db.connection.utils :as connection.utils]
              [mongo-db.core.errors      :as core.errors]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn command
  ; @description
  ; - Allows you to send commands to a database. It takes an optional database-name.
  ; - If a database reference is found, it attempts to execute the specified command
  ;   with the provided options.
  ;
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
   (if-let [database-reference (get @connection.state/REFERENCES database-name)]
           (try (mcr/command database-reference options)
                (catch Exception e (if warn? (println (str e "\n" {:options options})))))
           (try (throw (Exception. core.errors/NO-DATABASE-REFERENCE-FOUND-ERROR))
                (catch Exception e (println (str e "\n" {:database-name database-name})))))))


(ns mongo-db.core.env
    (:require [monger.core               :as mcr]
              [mongo-db.connection.env :as connection.env]
              [mongo-db.core.messages    :as core.messages]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn command
  ; @description
  ; - Allows you to send commands to a database. It takes an optional database name.
  ; - If a database reference is found, it attempts to execute the specified command with the provided options.
  ;
  ; @param (string)(opt) database-name
  ; @param (map) options
  ; {:warn? (boolean)(opt)
  ;   Default: true}
  ;
  ; @return (DBObject)
  ([options]
   (let [database-name (connection.env/get-default-database-name)]
        (command database-name options)))

  ([database-name {:keys [warn?] :or {warn? true} :as options}]
   (if-let [database-reference (connection.env/get-database-reference database-name)]
           (try (mcr/command database-reference options)
                (catch Exception e (if warn? (println (str e "\n" {:options options})))))
           (try (throw (Exception. core.messages/NO-DATABASE-REFERENCE-FOUND-ERROR))
                (catch Exception e (println (str e "\n" {:database-name database-name})))))))

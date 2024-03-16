
(ns mongo-db.connection.check
    (:require [mongo-db.core.env :as core.env]
              [mongo-db.connection.env :as connection.env]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn connected?
  ; @description
  ; - Checks whether a specific database has an active connection.
  ; - If no database name is passed it uses the connected database's name, in case
  ;   of only one database is connected.
  ;
  ; @param (string)(opt) database-name
  ;
  ; @usage
  ; (connected?)
  ;
  ; @usage
  ; (connected? "my-database")
  ;
  ; @return (boolean)
  ([]
   (let [database-name (connection.env/get-default-database-name)]
        (connected? database-name)))

  ([database-name]
   (core.env/command database-name {:ping 1 :warn? false})))

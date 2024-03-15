
(ns mongo-db.connection.env
    (:require [mongo-db.core.env :as core.env]
              [common-state.api :as common-state]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn get-connection-count
  ; @ignore
  ;
  ; @return (integer)
  []
  (-> (common-state/get-state :monger.extra :connections) keys count))

(defn get-first-database-name
  ; @ignore
  ;
  ; @return (string)
  []
  (-> (common-state/get-state :monger.extra :connections) keys first))

(defn get-default-database-name
  ; @ignore
  ;
  ; @description
  ; Returns the connected database's name as default database name in case
  ; of only one database is connected. Otherwise, it throws an error.
  ;
  ; @return (string)
  []
  (let [connection-count (get-connection-count)]
       (case connection-count 1 (get-first-database-name)
                              0 (throw (Exception. core.messages/MISSING-DATABASE-NAME-AND-NO-CONNECTION-ERROR))
                                (throw (Exception. core.messages/MISSING-DATABASE-NAME-AND-MULTI-CONNECTION-ERROR)))))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn connected?
  ; @description
  ; - Checks whether a specific database has an active connection.
  ; - If no database name is passed it uses the connected database's name, in case
  ;   of only one database is connected.
  ;
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
   (let [database-name (get-default-database-name)]
        (connected? database-name)))

  ([database-name]
   (core.env/command database-name {:ping 1 :warn? false})))

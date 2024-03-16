
(ns mongo-db.connection.env
    (:require [common-state.api :as common-state]
              [mongo-db.core.messages :as core.messages]))

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
                                (throw (Exception. core.messages/MISSING-DATABASE-NAME-AND-MULTIPLE-CONNECTIONS-ERROR)))))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn get-database-reference
  ; @param (string)(opt) database-name
  ;
  ; @usage
  ; (get-database-reference)
  ; =>
  ; ?
  ;
  ; @usage
  ; (get-database-reference "my-database")
  ; =>
  ; ?
  ;
  ; @return (?)
  ([]
   (let [database-name (get-default-database-name)]
        (get-database-reference database-name)))

  ([database-name]
   (common-state/get-state :monger.extra :connections database-name)))


(ns mongo-db.connection.env
    (:require [mongo-db.connection.state :as connection.state]
              [mongo-db.core.env         :as core.env]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn connected?
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
   (let [database-name (connection.utils/default-database-name)]
        (connected? database-name)))

  ([database-name]
   (core.env/command database-name {:ping 1 :warn? false})))

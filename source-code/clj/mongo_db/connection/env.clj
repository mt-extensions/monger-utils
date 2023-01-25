
(ns mongo-db.connection.env
    (:require [mongo-db.core.env :as core.env]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn connected?
  ; @usage
  ; (connected?)
  ;
  ; @return (boolean)
  []
  (core.env/command {:ping 1 :warn? false}))

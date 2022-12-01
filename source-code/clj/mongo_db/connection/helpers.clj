
(ns mongo-db.connection.helpers
    (:require [mongo-db.core.helpers :as core.helpers]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn connected?
  ; @usage
  ; (connected?)
  ;
  ; @return (boolean)
  []
  (core.helpers/command {:ping 1 :warn? false}))


(ns mongo-db.connection.subs
    (:require [re-frame.api :as r]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn- get-connection
  ; @return (com.mongodb.DB object)
  [db _]
  (get-in db [:mongo-db :connection/reference]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

; @usage
; [:mongo-db/get-connection]
(r/reg-sub :mongo-db/get-connection get-connection)


(ns mongo-db.connection.subs
    (:require [re-frame.api :as r :refer [r]]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn- get-connection
  ; @return (com.mongodb.DB object)
  [db _]
  (get-in db [:mongo-db :connection/reference]))

(defn- connected?
  ; @return (boolean)
  [db _]
  (r get-connection db))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(r/reg-sub :mongo-db/get-connection get-connection)
(r/reg-sub :mongo-db/connected?     connected?)

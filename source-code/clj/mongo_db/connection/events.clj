
(ns mongo-db.connection.events
    (:require [re-frame.api :as r]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn- store-reference!
  ; @param (com.mongodb.DB object) reference
  ;
  ; @return (map)
  [db [_ reference]]
  (assoc-in db [:mongo-db :connection/reference] reference))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(r/reg-event-db :mongo-db/store-reference! store-reference!)

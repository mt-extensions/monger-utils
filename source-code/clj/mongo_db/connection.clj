
;; -- Namespace ---------------------------------------------------------------
;; ----------------------------------------------------------------------------

(ns mongo-db.connection
    (:import  [com.mongodb MongoOptions ServerAddress])
    (:require [monger.core  :as mcr]
              [re-frame.api :as r :refer [r]]))



;; -- Subscriptions -----------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn- get-connection
  ; WARNING! NON-PUBLIC! DO NOT USE!
  ;
  ; @return (com.mongodb.DB object)
  [db _]
  (get-in db [:mongo-db :connection]))

(r/reg-sub :mongo-db/get-connection get-connection)

(defn- connected?
  ; WARNING! NON-PUBLIC! DO NOT USE!
  ;
  ; @return (boolean)
  [db _]
  (r get-connection db))

(r/reg-sub :mongo-db/connected? connected?)



;; -- DB events ---------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn- store-connection!
  ; WARNING! NON-PUBLIC! DO NOT USE!
  ;
  ; @param (com.mongodb.DB object) reference
  ;
  ; @return (map)
  [db [_ reference]]
  (assoc-in db [:mongo-db :connection] reference))

(r/reg-event-db :mongo-db/store-connection! store-connection!)



;; -- Side-effect events ------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn- build-connection!
  ; WARNING! NON-PUBLIC! DO NOT USE!
  ;
  ; @param (string) database-name
  ; @param (string) database-host
  ; @param (integer) database-port
  [database-name database-host database-port]
  (let [^MongoOptions  mongo-options  (mcr/mongo-options {:threads-allowed-to-block-for-connection-multiplier 300})
        ^ServerAddress server-address (mcr/server-address database-host  database-port)
                       connection     (mcr/connect        server-address mongo-options)
                       database       (mcr/get-db         connection     database-name)]
       (r/dispatch [:mongo-db/store-connection! database])))

(r/reg-fx :mongo-db/build-connection! build-connection!)

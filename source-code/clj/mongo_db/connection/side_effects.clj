
(ns mongo-db.connection.side-effects
    (:import  [com.mongodb MongoOptions ServerAddress])
    (:require [monger.core  :as mcr]
              [re-frame.api :as r]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn- build-connection!
  ; @param (string) database-name
  ; @param (string) database-host
  ; @param (integer) database-port
  [database-name database-host database-port]
  (let [^MongoOptions  mongo-options  (mcr/mongo-options {:threads-allowed-to-block-for-connection-multiplier 300})
        ^ServerAddress server-address (mcr/server-address database-host  database-port)
                       connection     (mcr/connect        server-address mongo-options)
                       database       (mcr/get-db         connection     database-name)]
       (r/dispatch [:mongo-db/store-connection! database])))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(r/reg-fx :mongo-db/build-connection! build-connection!)

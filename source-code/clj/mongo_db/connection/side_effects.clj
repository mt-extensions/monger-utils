
(ns mongo-db.connection.side-effects
    (:import  [com.mongodb MongoOptions ServerAddress])
    (:require [monger.core               :as mcr]
              [mongo-db.connection.state :as connection.state]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn build-connection!
  ; @param (string) database-name
  ; @param (string) database-host
  ; @param (integer) database-port
  ;
  ; @usage
  ; (build-connection! "my-database" "0.0.0.1" 4200)
  [database-name database-host database-port]
  (let [^MongoOptions  mongo-options  (mcr/mongo-options {:threads-allowed-to-block-for-connection-multiplier 300})
        ^ServerAddress server-address (mcr/server-address database-host  database-port)
                       connection     (mcr/connect        server-address mongo-options)
                       reference      (mcr/get-db         connection     database-name)]
       (reset! connection.state/REFERENCE reference)))


(ns mongo-db.actions.side-effects
    (:require monger.joda-time
              [monger.collection         :as mcl]
              [monger.operators          :refer :all]
              [mongo-db.connection.state :as connection.state]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn drop!
  ; @ignore
  ;
  ; @param (string) collection-name
  ;
  ; @return (?)
  [collection-name]
  (try (mcl/drop @connection.state/REFERENCE collection-name)
       (catch Exception e (println e (str e "\n" {:collection-name collection-name})))))

(defn insert-and-return!
  ; @ignore
  ;
  ; @param (string) collection-name
  ; @param (map) document
  ; {"_id" (org.bson.types.ObjectId object)}
  ;
  ; @return (namespaced map)
  [collection-name document]
  (try (mcl/insert-and-return @connection.state/REFERENCE collection-name document)
       (catch Exception e (println (str e "\n" {:collection-name collection-name :document document})))))

(defn save-and-return!
  ; @ignore
  ;
  ; @param (string) collection-name
  ; @param (namespaced map) document
  ; {"_id" (org.bson.types.ObjectId object)}
  ;
  ; @return (namespaced map)
  [collection-name document]
  (try (mcl/save-and-return @connection.state/REFERENCE collection-name document)
       (catch Exception e (println (str e "\n" {:collection-name collection-name :document document})))))

(defn remove-by-id!
  ; @ignore
  ;
  ; @param (string) collection-name
  ; @param (org.bson.types.ObjectId object) document-id
  ;
  ; @return (?)
  [collection-name document-id]
  (try (mcl/remove-by-id @connection.state/REFERENCE collection-name document-id)
       (catch Exception e (println (str e "\n" {:collection-name collection-name :document-id document-id})))))

(defn update!
  ; @ignore
  ;
  ; @param (string) collection-name
  ; @param (map) query
  ; {"_id" (org.bson.types.ObjectId object)(opt)}
  ; @param (map) document
  ; @param (map)(opt) options
  ; {:multi (boolean)(opt)
  ;   Default: false
  ;  :upsert (boolean)(opt)
  ;   Default: false}
  ;
  ; @return (com.mongodb.WriteResult object)
  ([collection-name query document]
   (update! collection-name query document {}))

  ([collection-name query document options]
   (try (mcl/update @connection.state/REFERENCE collection-name query document options)
        (catch Exception e (println (str e "\n" {:collection-name collection-name :query   query
                                                 :document        document        :options options}))))))

(defn upsert!
  ; @ignore
  ;
  ; @param (string) collection-name
  ; @param (map) query
  ; {"_id" (org.bson.types.ObjectId object)(opt)}
  ; @param (map) document
  ; @param (map)(opt) options
  ; {:multi (boolean)(opt)
  ;   Default: false}
  ;
  ; @return (com.mongodb.WriteResult object)
  ([collection-name query document]
   (upsert! collection-name query document {}))

  ([collection-name query document options]
   (try (mcl/upsert @connection.state/REFERENCE collection-name query document options)
        (catch Exception e (println (str e "\n" {:collection-name collection-name :query   query
                                                 :document        document        :options options}))))))

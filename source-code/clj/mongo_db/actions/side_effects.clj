
(ns mongo-db.actions.side-effects
    (:require monger.joda-time
              [monger.collection         :as mcl]
              [monger.operators          :refer :all]
              [mongo-db.connection.state :as connection.state]
              [mongo-db.connection.utils :as connection.utils]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn drop!
  ; @ignore
  ;
  ; @param (string) collection-path
  ;
  ; @return (?)
  [collection-path]
  (let [database-name      (connection.utils/collection-path->database-name   collection-path)
        collection-name    (connection.utils/collection-path->collection-name collection-path)
        database-reference (get @connection.state/REFERENCES database-name)]
       (try (mcl/drop database-reference collection-name)
            (catch Exception e (println e (str e "\n" {:collection-path collection-path}))))))

(defn insert-and-return!
  ; @ignore
  ;
  ; @param (string) collection-path
  ; @param (map) document
  ; {"_id" (org.bson.types.ObjectId object)}
  ;
  ; @return (namespaced map)
  [collection-path document]
  (let [database-name      (connection.utils/collection-path->database-name   collection-path)
        collection-name    (connection.utils/collection-path->collection-name collection-path)
        database-reference (get @connection.state/REFERENCES database-name)]
       (try (mcl/insert-and-return database-reference collection-name document)
            (catch Exception e (println (str e "\n" {:collection-path collection-path :document document}))))))

(defn save-and-return!
  ; @ignore
  ;
  ; @param (string) collection-path
  ; @param (namespaced map) document
  ; {"_id" (org.bson.types.ObjectId object)}
  ;
  ; @return (namespaced map)
  [collection-path document]
  (let [database-name      (connection.utils/collection-path->database-name   collection-path)
        collection-name    (connection.utils/collection-path->collection-name collection-path)
        database-reference (get @connection.state/REFERENCES database-name)]
       (try (mcl/save-and-return database-reference collection-name document)
            (catch Exception e (println (str e "\n" {:collection-path collection-path :document document}))))))

(defn remove-by-id!
  ; @ignore
  ;
  ; @param (string) collection-path
  ; @param (org.bson.types.ObjectId object) document-id
  ;
  ; @return (?)
  [collection-path document-id]
  (let [database-name      (connection.utils/collection-path->database-name   collection-path)
        collection-name    (connection.utils/collection-path->collection-name collection-path)
        database-reference (get @connection.state/REFERENCES database-name)]
       (try (mcl/remove-by-id database-reference collection-name document-id)
            (catch Exception e (println (str e "\n" {:collection-path collection-path :document-id document-id}))))))

(defn update!
  ; @ignore
  ;
  ; @param (string) collection-path
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
  ([collection-path query document]
   (update! collection-path query document {}))

  ([collection-path query document options]
   (let [database-name      (connection.utils/collection-path->database-name   collection-path)
         collection-name    (connection.utils/collection-path->collection-name collection-path)
         database-reference (get @connection.state/REFERENCES database-name)]
        (try (mcl/update database-reference collection-name query document options)
             (catch Exception e (println (str e "\n" {:collection-path collection-path :query   query
                                                      :document        document        :options options})))))))

(defn upsert!
  ; @ignore
  ;
  ; @param (string) collection-path
  ; @param (map) query
  ; {"_id" (org.bson.types.ObjectId object)(opt)}
  ; @param (map) document
  ; @param (map)(opt) options
  ; {:multi (boolean)(opt)
  ;   Default: false}
  ;
  ; @return (com.mongodb.WriteResult object)
  ([collection-path query document]
   (upsert! collection-path query document {}))

  ([collection-path query document options]
   (let [database-name      (connection.utils/collection-path->database-name   collection-path)
         collection-name    (connection.utils/collection-path->collection-name collection-path)
         database-reference (get @connection.state/REFERENCES database-name)]
        (try (mcl/upsert database-reference collection-name query document options)
             (catch Exception e (println (str e "\n" {:collection-path collection-path :query   query
                                                      :document        document        :options options})))))))

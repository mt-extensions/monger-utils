
(ns mongo-db.actions.side-effects
    (:require monger.joda-time
              [monger.collection         :as mcl]
              [monger.operators          :refer :all]
              [mongo-db.core.error :as core.error]
              [mongo-db.connection.env :as connection.env]
              [mongo-db.connection.utils :as connection.utils]
              [mongo-db.core.messages    :as core.messages]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn drop!
  ; @ignore
  ;
  ; @param (string) collection-path
  ;
  ; @return (?)
  [collection-path]
  (try (let [database-name   (connection.utils/collection-path->database-name   collection-path)
             collection-name (connection.utils/collection-path->collection-name collection-path)]
            (if-let [database-reference (connection.env/get-database-reference database-name)]
                    (mcl/drop database-reference collection-name)
                    (throw (Exception. core.messages/NO-DATABASE-REFERENCE-FOUND-ERROR))))
       (catch Exception e (core.error/error-catched e {:collection-path collection-path}))))

(defn insert-and-return!
  ; @ignore
  ;
  ; @param (string) collection-path
  ; @param (map) document
  ; {"_id" (org.bson.types.ObjectId object)}
  ;
  ; @return (namespaced map)
  [collection-path document]
  (try (let [database-name   (connection.utils/collection-path->database-name   collection-path)
             collection-name (connection.utils/collection-path->collection-name collection-path)]
            (if-let [database-reference (connection.env/get-database-reference database-name)]
                    (mcl/insert-and-return database-reference collection-name document)
                    (throw ;(Throwable. {:e core.messages/NO-DATABASE-REFERENCE-FOUND-ERROR :document document})
                           (ex-info "The ice cream has melted!"
                                    {:causes             #{:fridge-door-open :dangerously-high-temperature}
                                     :current-temperature {:value 25 :unit :celcius}}))))
       (catch Exception e (core.error/error-catched {:collection-path collection-path :document document :error e}))))

(defn save-and-return!
  ; @ignore
  ;
  ; @param (string) collection-path
  ; @param (namespaced map) document
  ; {"_id" (org.bson.types.ObjectId object)}
  ;
  ; @return (namespaced map)
  [collection-path document]
  (try (let [database-name   (connection.utils/collection-path->database-name   collection-path)
             collection-name (connection.utils/collection-path->collection-name collection-path)]
            (if-let [database-reference (connection.env/get-database-reference database-name)]
                    (mcl/save-and-return database-reference collection-name document)
                    (throw (Exception. core.messages/NO-DATABASE-REFERENCE-FOUND-ERROR))))
       (catch Exception e (core.error/error-catched e {:collection-path collection-path :document document}))))

(defn remove-by-id!
  ; @ignore
  ;
  ; @param (string) collection-path
  ; @param (org.bson.types.ObjectId object) document-id
  ;
  ; @return (?)
  [collection-path document-id]
  (try (let [database-name   (connection.utils/collection-path->database-name   collection-path)
             collection-name (connection.utils/collection-path->collection-name collection-path)]
            (if-let [database-reference (connection.env/get-database-reference database-name)]
                    (mcl/remove-by-id database-reference collection-name document-id)
                    (throw (Exception. core.messages/NO-DATABASE-REFERENCE-FOUND-ERROR))))
       (catch Exception e (core.error/error-catched e {:collection-path collection-path :document-id document-id}))))

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
   (try (let [database-name   (connection.utils/collection-path->database-name   collection-path)
              collection-name (connection.utils/collection-path->collection-name collection-path)]
             (if-let [database-reference (connection.env/get-database-reference database-name)]
                     (mcl/update database-reference collection-name query document options)
                     (throw (Exception. core.messages/NO-DATABASE-REFERENCE-FOUND-ERROR))))
        (catch Exception e (core.error/error-catched e {:collection-path collection-path :query query :document document :options options})))))

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
   (try (let [database-name   (connection.utils/collection-path->database-name   collection-path)
              collection-name (connection.utils/collection-path->collection-name collection-path)]
             (if-let [database-reference (connection.env/get-database-reference database-name)]
                     (mcl/upsert database-reference collection-name query document options)
                     (throw (Exception. core.messages/NO-DATABASE-REFERENCE-FOUND-ERROR))))
        (catch Exception e (core.error/error-catched e {:collection-path collection-path :query query :document document :options options})))))

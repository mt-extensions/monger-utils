
(ns mongo-db.reader.env
    (:require [monger.joda-time]
              [monger.collection         :as mcl]
              [monger.db                 :as mdb]
              [mongo-db.connection.state :as connection.state]
              [mongo-db.connection.utils :as connection.utils]
              [mongo-db.core.messages      :as core.messages]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn find-maps
  ; @ignore
  ;
  ; @param (string) collection-path
  ; @param (map) query
  ; @param (namespaced map)(opt) projection
  ;
  ; @return (namespaced maps in vector)
  ([collection-path query]
   (let [database-name   (connection.utils/collection-path->database-name   collection-path)
         collection-name (connection.utils/collection-path->collection-name collection-path)]
        (if-let [database-reference (get @connection.state/REFERENCES database-name)]
                (try (vec (mcl/find-maps database-reference collection-name query))
                     (catch Exception e (println (str e "\n" {:collection-path collection-path :query query}))))
                (try (throw (Exception. core.messages/NO-DATABASE-REFERENCE-FOUND-ERROR))
                     (catch Exception e (println (str e "\n" {:database-name database-name})))))))

  ([collection-path query projection]
   (let [database-name   (connection.utils/collection-path->database-name   collection-path)
         collection-name (connection.utils/collection-path->collection-name collection-path)]
        (if-let [database-reference (get @connection.state/REFERENCES database-name)]
                (try (vec (mcl/find-maps database-reference collection-name query projection))
                     (catch Exception e (println (str e "\n" {:collection-path collection-path :query query :projection projection}))))
                (try (throw (Exception. core.messages/NO-DATABASE-REFERENCE-FOUND-ERROR))
                     (catch Exception e (println (str e "\n" {:database-name database-name}))))))))

(defn find-one-as-map
  ; @ignore
  ;
  ; @param (string) collection-path
  ; @param (map) query
  ; @param (namespaced map)(opt) projection
  ;
  ; @return (namespaced map)
  ([collection-path query]
   (let [database-name   (connection.utils/collection-path->database-name   collection-path)
         collection-name (connection.utils/collection-path->collection-name collection-path)]
        (if-let [database-reference (get @connection.state/REFERENCES database-name)]
                (try (mcl/find-one-as-map database-reference collection-name query)
                     (catch Exception e (println (str e "\n" {:collection-path collection-path :query query}))))
                (try (throw (Exception. core.messages/NO-DATABASE-REFERENCE-FOUND-ERROR))
                     (catch Exception e (println (str e "\n" {:database-name database-name})))))))

  ([collection-path query projection]
   (let [database-name   (connection.utils/collection-path->database-name   collection-path)
         collection-name (connection.utils/collection-path->collection-name collection-path)]
        (if-let [database-reference (get @connection.state/REFERENCES database-name)]
                (try (mcl/find-one-as-map database-reference collection-name query projection)
                     (catch Exception e (println (str e "\n" {:collection-path collection-path :query query :projection projection}))))
                (try (throw (Exception. core.messages/NO-DATABASE-REFERENCE-FOUND-ERROR))
                     (catch Exception e (println (str e "\n" {:database-name database-name}))))))))

(defn find-map-by-id
  ; @ignore
  ;
  ; @param (string) collection-path
  ; @param (org.bson.types.ObjectId object) document-id
  ; @param (namespaced map)(opt) projection
  ;
  ; @return (namespaced map)
  ([collection-path document-id]
   (let [database-name   (connection.utils/collection-path->database-name   collection-path)
         collection-name (connection.utils/collection-path->collection-name collection-path)]
        (if-let [database-reference (get @connection.state/REFERENCES database-name)]
                (try (mcl/find-map-by-id database-reference collection-name document-id)
                     (catch Exception e (println (str e "\n" {:collection-path collection-path :document-id document-id}))))
                (try (throw (Exception. core.messages/NO-DATABASE-REFERENCE-FOUND-ERROR))
                     (catch Exception e (println (str e "\n" {:database-name database-name})))))))

  ([collection-path document-id projection]
   (let [database-name   (connection.utils/collection-path->database-name   collection-path)
         collection-name (connection.utils/collection-path->collection-name collection-path)]
        (if-let [database-reference (get @connection.state/REFERENCES database-name)]
                (try (mcl/find-map-by-id database-reference collection-name document-id projection)
                     (catch Exception e (println (str e "\n" {:collection-path collection-path :document-id document-id :projection projection}))))
                (try (throw (Exception. core.messages/NO-DATABASE-REFERENCE-FOUND-ERROR))
                     (catch Exception e (println (str e "\n" {:database-name database-name}))))))))

(defn count-documents
  ; @ignore
  ;
  ; @param (string) collection-path
  ;
  ; @return (integer)
  [collection-path]
  (let [database-name   (connection.utils/collection-path->database-name   collection-path)
        collection-name (connection.utils/collection-path->collection-name collection-path)]
       (if-let [database-reference (get @connection.state/REFERENCES database-name)]
               (try (mcl/count database-reference collection-name)
                    (catch Exception e (println (str e "\n" {:collection-path collection-path}))))
               (try (throw (Exception. core.messages/NO-DATABASE-REFERENCE-FOUND-ERROR))
                    (catch Exception e (println (str e "\n" {:database-name database-name})))))))


(defn count-documents-by-query
  ; @ignore
  ;
  ; @param (string) collection-path
  ; @param (map) query
  ;
  ; @return (integer)
  [collection-path query]
  (let [database-name   (connection.utils/collection-path->database-name   collection-path)
        collection-name (connection.utils/collection-path->collection-name collection-path)]
       (if-let [database-reference (get @connection.state/REFERENCES database-name)]
               (try (mcl/count database-reference collection-name query)
                    (catch Exception e (println (str e "\n" {:collection-path collection-path :query query}))))
               (try (throw (Exception. core.messages/NO-DATABASE-REFERENCE-FOUND-ERROR))
                    (catch Exception e (println (str e "\n" {:database-name database-name})))))))

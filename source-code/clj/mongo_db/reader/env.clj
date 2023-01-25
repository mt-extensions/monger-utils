
(ns mongo-db.reader.env
    (:require monger.joda-time
              [monger.collection         :as mcl]
              [monger.db                 :as mdb]
              [mongo-db.connection.state :as connection.state]))


;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn find-maps
  ; @ignore
  ;
  ; @param (string) collection-name
  ; @param (map) query
  ; @param (namespaced map)(opt) projection
  ;
  ; @return (namespaced maps in vector)
  ([collection-name query]
   (try (vec (mcl/find-maps @connection.state/REFERENCE collection-name query))
        (catch Exception e (println (str e "\n" {:collection-name collection-name :query query})))))

  ([collection-name query projection]
   (try (vec (mcl/find-maps @connection.state/REFERENCE collection-name query projection))
        (catch Exception e (println (str e "\n" {:collection-name collection-name :query query :projection projection}))))))

(defn find-one-as-map
  ; @ignore
  ;
  ; @param (string) collection-name
  ; @param (map) query
  ; @param (namespaced map)(opt) projection
  ;
  ; @return (namespaced map)
  ([collection-name query]
   (try (mcl/find-one-as-map @connection.state/REFERENCE collection-name query)
        (catch Exception e (println (str e "\n" {:collection-name collection-name :query query})))))

  ([collection-name query projection]
   (try (mcl/find-one-as-map @connection.state/REFERENCE collection-name query projection)
        (catch Exception e (println (str e "\n" {:collection-name collection-name :query query :projection projection}))))))

(defn find-map-by-id
  ; @ignore
  ;
  ; @param (string) collection-name
  ; @param (org.bson.types.ObjectId object) document-id
  ; @param (namespaced map)(opt) projection
  ;
  ; @return (namespaced map)
  ([collection-name document-id]
   (try (mcl/find-map-by-id @connection.state/REFERENCE collection-name document-id)
        (catch Exception e (println (str e "\n" {:collection-name collection-name :document-id document-id})))))

  ([collection-name document-id projection]
   (try (mcl/find-map-by-id @connection.state/REFERENCE collection-name document-id projection)
        (catch Exception e (println (str e "\n" {:collection-name collection-name :document-id document-id :projection projection}))))))

(defn count-documents
  ; @ignore
  ;
  ; @param (string) collection-name
  ;
  ; @return (integer)
  [collection-name]
  (try (mcl/count @connection.state/REFERENCE collection-name)
       (catch Exception e (println (str e "\n" {:collection-name collection-name})))))

(defn count-documents-by-query
  ; @ignore
  ;
  ; @param (string) collection-name
  ; @param (map) query
  ;
  ; @return (integer)
  [collection-name query]
  (try (mcl/count @connection.state/REFERENCE collection-name query)
       (catch Exception e (println (str e "\n" {:collection-name collection-name :query query})))))

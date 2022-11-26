
(ns mongo-db.reader.helpers
    (:require monger.joda-time
              [monger.collection :as mcl]
              [monger.db         :as mdb]
              [re-frame.api      :as r]))

;; -- Error handling ----------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn find-maps
  ; @param (string) collection-name
  ; @param (map) query
  ; @param (namespaced map)(opt) projection
  ;
  ; @return (namespaced maps in vector)
  ([collection-name query]
   (let [database @(r/subscribe [:mongo-db/get-connection])]
        (try (vec (mcl/find-maps database collection-name query))
             (catch Exception e (println (str e "\n" {:collection-name collection-name :query query}))))))

  ([collection-name query projection]
   (let [database @(r/subscribe [:mongo-db/get-connection])]
        (try (vec (mcl/find-maps database collection-name query projection))
             (catch Exception e (println (str e "\n" {:collection-name collection-name :query query :projection projection})))))))

(defn find-one-as-map
  ; @param (string) collection-name
  ; @param (map) query
  ; @param (namespaced map)(opt) projection
  ;
  ; @return (namespaced map)
  ([collection-name query]
   (let [database @(r/subscribe [:mongo-db/get-connection])]
        (try (mcl/find-one-as-map database collection-name query)
             (catch Exception e (println (str e "\n" {:collection-name collection-name :query query}))))))

  ([collection-name query projection]
   (let [database @(r/subscribe [:mongo-db/get-connection])]
        (try (mcl/find-one-as-map database collection-name query projection)
             (catch Exception e (println (str e "\n" {:collection-name collection-name :query query :projection projection})))))))

(defn find-map-by-id
  ; @param (string) collection-name
  ; @param (org.bson.types.ObjectId object) document-id
  ; @param (namespaced map)(opt) projection
  ;
  ; @return (namespaced map)
  ([collection-name document-id]
   (let [database @(r/subscribe [:mongo-db/get-connection])]
        (try (mcl/find-map-by-id database collection-name document-id)
             (catch Exception e (println (str e "\n" {:collection-name collection-name :document-id document-id}))))))

  ([collection-name document-id projection]
   (let [database @(r/subscribe [:mongo-db/get-connection])]
        (try (mcl/find-map-by-id database collection-name document-id projection)
             (catch Exception e (println (str e "\n" {:collection-name collection-name :document-id document-id :projection projection})))))))

(defn count-documents
  ; @param (string) collection-name
  ;
  ; @return (integer)
  [collection-name]
  (let [database @(r/subscribe [:mongo-db/get-connection])]
       (try (mcl/count database collection-name)
            (catch Exception e (println (str e "\n" {:collection-name collection-name}))))))

(defn count-documents-by-query
  ; @param (string) collection-name
  ; @param (map) query
  ;
  ; @return (integer)
  [collection-name query]
  (let [database @(r/subscribe [:mongo-db/get-connection])]
       (try (mcl/count database collection-name query)
            (catch Exception e (println (str e "\n" {:collection-name collection-name :query query}))))))

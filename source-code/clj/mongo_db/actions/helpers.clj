
(ns mongo-db.actions.helpers
    (:require monger.joda-time
              [monger.collection :as mcl]
              [monger.operators  :refer :all]
              [re-frame.api      :as r]))

;; -- Error handling ----------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn drop!
  ; @param (string) collection-name
  ;
  ; @return (?)
  [collection-name]
  (let [database @(r/subscribe [:mongo-db/get-connection])]
       (try (mcl/drop database collection-name)
            (catch Exception e (println e (str e "\n" {:collection-name collection-name}))))))

(defn insert-and-return!
  ; @param (string) collection-name
  ; @param (map) document
  ; {"_id" (org.bson.types.ObjectId object)}
  ;
  ; @return (namespaced map)
  [collection-name document]
  (let [database @(r/subscribe [:mongo-db/get-connection])]
       (try (mcl/insert-and-return database collection-name document)
            (catch Exception e (println (str e "\n" {:collection-name collection-name :document document}))))))

(defn save-and-return!
  ; @param (string) collection-name
  ; @param (namespaced map) document
  ; {"_id" (org.bson.types.ObjectId object)}
  ;
  ; @return (namespaced map)
  [collection-name document]
  (let [database @(r/subscribe [:mongo-db/get-connection])]
       (try (mcl/save-and-return database collection-name document)
            (catch Exception e (println (str e "\n" {:collection-name collection-name :document document}))))))

(defn remove-by-id!
  ; @param (string) collection-name
  ; @param (org.bson.types.ObjectId object) document-id
  ;
  ; @return (?)
  [collection-name document-id]
  (let [database @(r/subscribe [:mongo-db/get-connection])]
       (try (mcl/remove-by-id database collection-name document-id)
            (catch Exception e (println (str e "\n" {:collection-name collection-name :document-id document-id}))))))

(defn update!
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
   (let [database @(r/subscribe [:mongo-db/get-connection])]
        (try (mcl/update database collection-name query document options)
             (catch Exception e (println (str e "\n" {:collection-name collection-name :query   query
                                                      :document        document        :options options})))))))

(defn upsert!
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
   (let [database @(r/subscribe [:mongo-db/get-connection])]
        (try (mcl/upsert database collection-name query document options)
             (catch Exception e (println (str e "\n" {:collection-name collection-name :query   query
                                                      :document        document        :options options})))))))

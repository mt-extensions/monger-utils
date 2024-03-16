
(ns mongo-db.core.error
    (:require [fruits.map.api :as map]
              [mongo-db.connection.utils :as connection.utils]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn error-catched
  ; @ignore
  ;
  ; @param (map) details
  [details]
  (when (map/contains-key? details :collection-path)
        (println "database-name:"   (-> details :collection-path connection.utils/collection-path->database-name))
        (println "collection-name:" (-> details :collection-path connection.utils/collection-path->collection-name)))
  (doseq [[k v] details] (println (-> k name (str ":")) v)))


;; -- Namespace ---------------------------------------------------------------
;; ----------------------------------------------------------------------------

(ns mongo-db.aggregation
    (:require [monger.core         :as mcr]
              [mongo-db.adaptation :as adaptation]
              [mongo-db.config     :as config]
              [re-frame.api        :as r]))



;; -- Error handling ----------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn- command
  ; @param (map) options
  ;
  ; @return (DBObject)
  [options]
  (let [database @(r/subscribe [:mongo-db/get-connection])]
       (try (mcr/command database options)
            (catch Exception e (println (str e "\n" {:options options}))))))



;; -- Aggregation functions ---------------------------------------------------
;; ----------------------------------------------------------------------------

(defn process
  ; @param (string) collection-name
  ; @param (maps in vector) pipeline
  ; @param (map)(opt) options
  ;  {:locale (string)(opt)
  ;    Default: config/DEFAULT-LOCALE}
  ;
  ; @return (maps in vector)
  ([collection-name pipeline]
   (process collection-name pipeline {:locale config/DEFAULT-LOCALE}))

  ([collection-name pipeline {:keys [locale]}]
   (if-let [db-object (command {:aggregate collection-name
                                :pipeline  pipeline
                                :collation {:locale locale :numericOrdering true}
                                :cursor    {}})]
           (adaptation/aggregation-output db-object))))

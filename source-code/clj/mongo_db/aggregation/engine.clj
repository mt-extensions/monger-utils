
(ns mongo-db.aggregation.engine
    (:require [mongo-db.aggregation.adaptation :as aggregation.adaptation]
              [mongo-db.connection.utils       :as connection.utils]
              [mongo-db.core.config            :as core.config]
              [mongo-db.core.env               :as core.env]))

;; -- Aggregation functions ---------------------------------------------------
;; ----------------------------------------------------------------------------

(defn process
  ; @param (string) collection-path
  ; @param (maps in vector) pipeline
  ; @param (map)(opt) options
  ; {:locale (string)(opt)
  ;   Default: core.config/DEFAULT-LOCALE}
  ;
  ; @return (maps in vector)
  ([collection-path pipeline]
   (process collection-path pipeline {}))

  ([collection-path pipeline {:keys [locale] :or {locale core.config/DEFAULT-LOCALE}}]
   (let [database-name   (connection.utils/collection-path->database-name   collection-path)
         collection-name (connection.utils/collection-path->collection-name collection-path)]
        (if-let [db-object (core.env/command database-name {:aggregate collection-name
                                                            :pipeline  pipeline
                                                            :collation {:locale locale :numericOrdering true}
                                                            :cursor    {}})]
                (aggregation.adaptation/aggregation-output db-object)))))

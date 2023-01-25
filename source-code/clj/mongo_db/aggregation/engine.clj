
(ns mongo-db.aggregation.engine
    (:require [mongo-db.aggregation.adaptation :as aggregation.adaptation]
              [mongo-db.core.config            :as core.config]
              [mongo-db.core.env               :as core.env]))

;; -- Aggregation functions ---------------------------------------------------
;; ----------------------------------------------------------------------------

(defn process
  ; @param (string) collection-name
  ; @param (maps in vector) pipeline
  ; @param (map)(opt) options
  ; {:locale (string)(opt)
  ;   Default: core.config/DEFAULT-LOCALE}
  ;
  ; @return (maps in vector)
  ([collection-name pipeline]
   (process collection-name pipeline {}))

  ([collection-name pipeline {:keys [locale] :or {locale core.config/DEFAULT-LOCALE}}]
   (if-let [db-object (core.env/command {:aggregate collection-name
                                         :pipeline  pipeline
                                         :collation {:locale locale :numericOrdering true}
                                         :cursor    {}})]
           (aggregation.adaptation/aggregation-output db-object))))


(ns mongo-db.aggregation.engine
    (:require [mongo-db.aggregation.adaptation :as aggregation.adaptation]
              [mongo-db.aggregation.helpers    :as aggregation.helpers]
              [mongo-db.core.config            :as core.config]))

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
   (process collection-name pipeline {:locale core.config/DEFAULT-LOCALE}))

  ([collection-name pipeline {:keys [locale]}]
   (if-let [db-object (aggregation.helpers/command {:aggregate collection-name
                                                    :pipeline  pipeline
                                                    :collation {:locale locale :numericOrdering true}
                                                    :cursor    {}})]
           (aggregation.adaptation/aggregation-output db-object))))

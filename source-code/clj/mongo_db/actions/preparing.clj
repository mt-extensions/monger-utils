
(ns mongo-db.actions.preparing
    (:require [gestures.api           :as gestures]
              [keyword.api            :as keyword]
              [map.api                :as map]
              [mongo-db.core.errors   :as core.errors]
              [mongo-db.core.utils    :as core.utils]
              [mongo-db.reader.engine :as reader.engine]
              [noop.api               :refer [return]]))

;; -- Inserting document ------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn- ordered-insert-input
  ; @ignore
  ;
  ; @param (string) collection-path
  ; @param (namespaced map) document
  ; {:namespace/order (integer)(opt)}
  ; @param (map) options
  ;
  ; @return (namespaced map)
  ; {:namespace/order (integer)}
  [collection-path document _]
  ; Az upsert-input, save-input, ... függvények is az insert-input függvényt használják
  ; a dokumentum előkészítésére ezért ha a dokumentum már rendelkezik namespace/order
  ; értékkel, akkor nem változtat rajta.
  (if-let [namespace (map/get-namespace document)]
          (let [order-key  (keyword/add-namespace :order namespace)
                last-order (reader.engine/get-all-document-count collection-path)]
               (merge {order-key last-order} document))
          (throw (Exception. core.errors/MISSING-NAMESPACE-ERROR))))

(defn insert-input
  ; @ignore
  ;
  ; @param (string) collection-path
  ; @param (namespaced map) document
  ; @param (map) options
  ; {:ordered? (boolean)(opt)
  ;   Default: false
  ;  :prepare-f (function)(opt)}
  ;
  ; @return (namespaced map)
  ; {:namespace/order (integer)}
  [collection-path document {:keys [ordered? prepare-f] :as options}]
  (try (as-> document % (if-not ordered?  % (ordered-insert-input collection-path % options))
                        (if-not prepare-f % (prepare-f %)))
       (catch Exception e (println (str e "\n" {:collection-path collection-path :document document :options options})))))

;; -- Saving document ---------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn save-input
  ; @ignore
  ;
  ; @param (string) collection-path
  ; @param (namespaced map) document
  ; @param (map) options
  ; {:ordered? (boolean)(opt)
  ;   Default: false
  ;  :prepare-f (function)(opt)}
  ;
  ; @return (namespaced map)
  ; {:namespace/order (integer)}
  [collection-path document options]
  (insert-input collection-path document options))

;; -- Updating document -------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn update-input
  ; @ignore
  ;
  ; @param (string) collection-path
  ; @param (namespaced map) document
  ; @param (map) options
  ; {:prepare-f (function)(opt)}
  ;
  ; @return (namespaced map)
  [collection-path document {:keys [prepare-f] :as options}]
  (try (if prepare-f (prepare-f document)
                     (return    document))
       (catch Exception e (println (str e "\n" {:collection-path collection-path :document document :options options})))))

;; -- Upserting document ------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn upsert-input
  ; @ignore
  ;
  ; @param (string) collection-path
  ; @param (namespaced map) document
  ; @param (map) options
  ; {:prepare-f (function)(opt)}
  ;
  ; @return (namespaced map)
  ; {:namespace/order (integer)}
  [collection-path document options]
  (insert-input collection-path document options))

;; -- Applying document -------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn apply-input
  ; @ignore
  ;
  ; @param (string) collection-path
  ; @param (namespaced map) document
  ; @param (map) options
  ; {:prepare-f (function)(opt)}
  ;
  ; @return (namespaced map)
  [collection-path document options]
  (update-input collection-path document options))

;; -- Duplicating document ----------------------------------------------------
;; ----------------------------------------------------------------------------

(defn- changed-duplicate-input
  ; @ignore
  ;
  ; @param (string) collection-path
  ; @param (namespaced map) document
  ; @param (map) options
  ; {:changes (namespaced map)}
  ;
  ; @return (string)
  [_ document {:keys [changes]}]
  ; Ha a dokumentum kliens-oldali változata esetlegesen el nem mentett változtatásokat tartalmaz,
  ; akkor a változtatások a {:changes ...} tulajdonság értékeként megadhatók és a dokumentumról
  ; készülő másolat tartalmazni fogja őket.
  (merge document changes))

(defn- labeled-duplicate-input
  ; @ignore
  ;
  ; @param (string) collection-path
  ; @param (namespaced map) document
  ; @param (map) options
  ; {:label-key (namespaced keyword)}
  ;
  ; @return (string)
  [collection-path document {:keys [label-key]}]
  (let [collection          (reader.engine/get-collection collection-path)
        document-label      (get  document label-key)
        all-document-labels (mapv label-key collection)
        copy-label (gestures/item-label->copy-label document-label all-document-labels)]
       (assoc document label-key copy-label)))

(defn- ordered-duplicate-input
  ; @ignore
  ;
  ; @param (string) collection-path
  ; @param (namespaced map) document
  ; @param (map) options
  ;
  ; @return (namespaced map)
  ; {:namespace/order (integer)}
  [_ document _]
  (if-let [namespace (map/get-namespace document)]
          (let [order-key (keyword/add-namespace :order namespace)]
               (if-let [order (get document order-key)]
                       (update document order-key inc)
                       (throw (Exception. core.errors/MISSING-DOCUMENT-ORDER-ERROR))))
          (throw (Exception. core.errors/MISSING-NAMESPACE-ERROR))))

(defn duplicate-input
  ; @ignore
  ;
  ; @param (string) collection-path
  ; @param (namespaced map) document
  ; @param (map) options
  ; {:changes (namespaced map)(opt)
  ;  :label-key (namespaced keyword)(opt)
  ;  :ordered? (boolean)(opt)
  ;   Default: false
  ;  :prepare-f (function)(opt)}
  ;
  ; @return (namespaced map)
  ; {:namespace/order (integer)}
  [collection-path document {:keys [changes label-key ordered? prepare-f] :as options}]
  (try (as-> document % (if-not prepare-f % (prepare-f %))
                        (if-not changes   % (changed-duplicate-input collection-path % options))
                        (if-not label-key % (labeled-duplicate-input collection-path % options))
                        (if-not ordered?  % (ordered-duplicate-input collection-path % options))
                        ; A dokumentum a changes térképpel való összefésülés után kapja meg a másolat azonosítóját,
                        ; így nem okoz hibát, ha a changes térkép tartalmazza az eredeti azonosítót
                        (core.utils/assoc-id %))
       (catch Exception e (println (str e "\n" {:collection-path collection-path :document document :options options})))))

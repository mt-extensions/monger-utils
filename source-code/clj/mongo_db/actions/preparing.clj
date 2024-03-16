
(ns mongo-db.actions.preparing
    (:require [fruits.gestures.api    :as gestures]
              [fruits.keyword.api     :as keyword]
              [fruits.map.api         :as map]
              [mongo-db.core.messages :as core.messages]
              [mongo-db.core.utils    :as core.utils]
              [mongo-db.reader.engine :as reader.engine]))

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
  ; The 'upsert-input', 'save-input', and other functions also use the 'insert-input'
  ; function to prepare the given document, so if the document already has a ':namespace/order'
  ; value, it does not change that value.
  (if-let [namespace (map/namespace document)]
          (let [order-key  (keyword/add-namespace :order namespace)
                last-order (reader.engine/get-all-document-count collection-path)]
               (merge {order-key last-order} document))
          (throw (Exception. core.messages/DOCUMENT-TYPE-ERROR))))

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
  (try (if prepare-f (-> document prepare-f)
                     (-> document))
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
  ; If the client-side version of the document contains unsaved changes,
  ; these changes can be provided as values of the {:changes ...} property,
  ; and the copy document will include them.
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
  (if-let [namespace (map/namespace document)]
          (let [order-key (keyword/add-namespace :order namespace)]
               (if-let [order (get document order-key)]
                       (update document order-key inc)
                       (throw (Exception. core.messages/MISSING-DOCUMENT-ORDER-ERROR))))
          (throw (Exception. core.messages/DOCUMENT-TYPE-ERROR))))

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
                        ; The copy document gets its ID after it has been merged with the given ':changes' map.
                        ; Therefore, it wouldn't cause any problem if the ':changes' map also contains an ID.
                        (core.utils/assoc-id %))
       (catch Exception e (println (str e "\n" {:collection-path collection-path :document document :options options})))))

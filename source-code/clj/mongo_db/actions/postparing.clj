
(ns mongo-db.actions.postparing
    (:require [mongo-db.core.error :as core.error]))

;; -- Applying document -------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn apply-input
  ; @ignore
  ;
  ; @param (string) collection-path
  ; @param (namespaced map) document
  ; @param (map) options
  ; {:postpare-f (function)(opt)}
  ;
  ; @return (namespaced map)
  [collection-path document {:keys [postpare-f] :as options}]
  (try (if postpare-f (-> document postpare-f)
                      (-> document))
       (catch Exception e (core.error/error-catched e {:collection-path collection-path :document document :options options}))))

;; -- Duplicating document -----------------------------------------------------
;; ----------------------------------------------------------------------------

(defn duplicate-input
  ; @ignore
  ;
  ; @param (string) collection-path
  ; @param (namespaced map) document
  ; @param (map) options
  ; {:postpare-f (function)(opt)}
  ;
  ; @return (namespaced map)
  [collection-path document {:keys [postpare-f] :as options}]
  (try (if postpare-f (-> document postpare-f)
                      (-> document))
       (catch Exception e (core.error/error-catched e {:collection-path collection-path :document document :options options}))))

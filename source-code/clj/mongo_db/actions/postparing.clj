
(ns mongo-db.actions.postparing
    (:require [noop.api :refer [return]]))

;; -- Applying document -------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn apply-input
  ; @ignore
  ;
  ; @param (string) collection-name
  ; @param (namespaced map) document
  ; @param (map) options
  ; {:postpare-f (function)(opt)}
  ;
  ; @return (namespaced map)
  [collection-name document {:keys [postpare-f] :as options}]
  (try (if postpare-f (postpare-f document)
                      (return     document))
       (catch Exception e (println (str e "\n" {:collection-name collection-name :document document :options options})))))

;; -- Duplicating document -----------------------------------------------------
;; ----------------------------------------------------------------------------

(defn duplicate-input
  ; @ignore
  ;
  ; @param (string) collection-name
  ; @param (namespaced map) document
  ; @param (map) options
  ; {:postpare-f (function)(opt)}
  ;
  ; @return (namespaced map)
  [collection-name document {:keys [postpare-f] :as options}]
  (try (if postpare-f (postpare-f document)
                      (return     document))
       (catch Exception e (println (str e "\n" {:collection-name collection-name :document document :options options})))))

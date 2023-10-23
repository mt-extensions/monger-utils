
(ns mongo-db.actions.postparing)

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
       (catch Exception e (println (str e "\n" {:collection-path collection-path :document document :options options})))))

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
       (catch Exception e (println (str e "\n" {:collection-path collection-path :document document :options options})))))

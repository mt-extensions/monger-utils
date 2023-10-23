
(ns mongo-db.actions.prototyping)

;; -- Inserting document ------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn insert-input
  ; @ignore
  ;
  ; @param (string) collection-path
  ; @param (*) document
  ; @param (map) options
  ; {:prototype-f (function)(opt)}
  ;
  ; @return (*)
  [collection-path document {:keys [prototype-f] :as options}]
  (try (if prototype-f (-> document prototype-f)
                       (-> document))
       (catch Exception e (println (str e "\n" {:collection-path collection-path :document document :options options})))))

;; -- Saving document ---------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn save-input
  ; @ignore
  ;
  ; @param (string) collection-path
  ; @param (*) document
  ; @param (map) options
  ; {:prototype-f (function)(opt)}
  ;
  ; @return (*)
  [collection-path document options]
  (insert-input collection-path document options))

;; -- Updating document -------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn update-input
  ; @ignore
  ;
  ; @param (string) collection-path
  ; @param (*) document
  ; @param (map) options
  ; {:prototype-f (function)(opt)}
  ;
  ; @return (*)
  [collection-path document options]
  (insert-input collection-path document options))

;; -- Upserting document ------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn upsert-input
  ; @ignore
  ;
  ; @param (string) collection-path
  ; @param (*) document
  ; @param (map) options
  ; {:prototype-f (function)(opt)}
  ;
  ; @return (*)
  [collection-path document options]
  (insert-input collection-path document options))

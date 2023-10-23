
(ns mongo-db.actions.checking
    (:require [map.api                  :as map]
              [mongo-db.core.errors     :as core.errors]
              [mongo-db.reader.checking :as reader.checking]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn insert-input
  ; @ignore
  ;
  ; @param (*) document
  ;
  ; @return (*)
  [document]
  (try (if-let [namespace (map/get-namespace document)]
               (-> document)
               (throw (Exception. core.errors/MISSING-NAMESPACE-ERROR)))
       (catch Exception e (println (str e "\n" {:document document})))))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn save-input
  ; @ignore
  ;
  ; @param (*) document
  ;
  ; @return (*)
  [document]
  (try (if-let [namespace (map/get-namespace document)]
               (-> document)
               (throw (Exception. core.errors/MISSING-NAMESPACE-ERROR)))
       (catch Exception e (println (str e "\n" {:document document})))))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn update-input
  ; @ignore
  ;
  ; @param (*) document
  ;
  ; @return (*)
  [document]
  ; Az update művelet számára névtéres dokumentumot vagy utasításokat
  ; tartalmazó névtér nélküli térképet is lehetséges átadni.
  ; Pl.: {:$inc {:namespace/my-integer 1}}
  (try (if (-> document map?)
           (-> document)
           (throw (Exception. core.errors/INPUT-MUST-BE-MAP-ERROR)))
       (catch Exception e (println (str e "\n" {:document document})))))

(defn update-query
  ; @ignore
  ;
  ; @param (*) query
  ;
  ; @return (*)
  [query]
  (reader.checking/find-query query))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn upsert-input
  ; @ignore
  ;
  ; @param (*) document
  ;
  ; @return (*)
  [document]
  (update-input document))

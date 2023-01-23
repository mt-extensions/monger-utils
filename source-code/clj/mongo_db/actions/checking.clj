
(ns mongo-db.actions.checking
    (:require [map.api                  :as map]
              [mongo-db.core.errors     :as core.errors]
              [mongo-db.reader.checking :as reader.checking]
              [noop.api                 :refer [return]]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn insert-input
  ; @param (*) document
  ;
  ; @return (*)
  [document]
  (try (if-let [namespace (map/get-namespace document)]
               (return document)
               (throw (Exception. core.errors/MISSING-NAMESPACE-ERROR)))
       (catch Exception e (println (str e "\n" {:document document})))))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn save-input
  ; @param (*) document
  ;
  ; @return (*)
  [document]
  (try (if-let [namespace (map/get-namespace document)]
               (return document)
               (throw (Exception. core.errors/MISSING-NAMESPACE-ERROR)))
       (catch Exception e (println (str e "\n" {:document document})))))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn update-input
  ; @param (*) document
  ;
  ; @return (*)
  [document]
  ; Az update művelet számára névtéres dokumentumot vagy utasításokat
  ; tartalmazó névtér nélküli térképet is lehetséges átadni.
  ; Pl.: {:$inc {:namespace/my-integer 1}}
  (try (if (map?   document)
           (return document)
           (throw (Exception. core.errors/INPUT-MUST-BE-MAP-ERROR)))
       (catch Exception e (println (str e "\n" {:document document})))))

(defn update-query
  ; @param (*) query
  ;
  ; @return (*)
  [query]
  (reader.checking/find-query query))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn upsert-input
  ; @param (*) document
  ;
  ; @return (*)
  [document]
  (update-input document))

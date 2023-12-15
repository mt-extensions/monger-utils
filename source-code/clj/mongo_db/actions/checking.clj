
(ns mongo-db.actions.checking
    (:require [fruits.map.api           :as map]
              [mongo-db.core.messages     :as core.messages]
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
  (try (if (-> document map/namespaced?)
           (-> document)
           (throw (Exception. core.messages/MISSING-NAMESPACE-ERROR)))
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
  (try (if (-> document map/namespaced?)
           (-> document)
           (throw (Exception. core.messages/MISSING-NAMESPACE-ERROR)))
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
  ; The 'update' action could take a namespaced document or a non-namespaced map
  ; with document commands.
  ; E.g., {:$inc {:namespace/my-integer 1}}
  (try (if (-> document map?)
           (-> document)
           (throw (Exception. core.messages/INPUT-MUST-BE-MAP-ERROR)))
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

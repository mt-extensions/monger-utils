
(ns mongo-db.reader.prototyping)

;; -- Find document -----------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn find-output
  ; @ignore
  ;
  ; @param (string) collection-path
  ; @param (namespaced map) document
  ; @param (map) options
  ; {:prototype-f (function)(opt)}
  ;
  ; @return (*)
  [collection-path document {:keys [prototype-f] :as options}]
  (try (if prototype-f (-> document prototype-f)
                       (-> document))
       (catch Exception e (println (str e "\n" {:collection-path collection-path :document document :options options})))))

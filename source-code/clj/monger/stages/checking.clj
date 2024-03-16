
(ns monger.stages.checking
    (:require [fruits.map.api :as map]
              [monger.stages.messages :as messages]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn check-document
  ; @description
  ; - Returns the given document if provided as a map, otherwise throws an error.
  ; - Optionally checks whether it is namespaced.
  ;
  ; @param (*) document
  ; @param (map) options
  ; {:check-ns? (boolean)(opt)}
  ;
  ; @usage
  ; (check-document {:my-namespace/id "MyObjectId"}
  ;                 {:require-namespace? true})
  ; =>
  ; {:my-namespace/id "MyObjectId"}
  ;
  ; @return (*)
  [document {:keys [check-ns?]}]
  (if check-ns? (if (-> document map/namespaced?)
                    (-> document)
                    (-> messages/NAMESPACED-DOCUMENT-TYPE-ERROR (ex-info {:document document}) throw))
                (if (-> document map?)
                    (-> document)
                    (-> messages/DOCUMENT-TYPE-ERROR (ex-info {:document document}) throw))))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn check-query
  ; @description
  ; Returns the given query if provided as a map, otherwise throws an error.
  ;
  ; @param (*) query
  ;
  ; @usage
  ; (check-query {:my-namespace/my-string "my-value"
  ;               :$or [{:my-namespace/id "MyObjectId"}]})
  ;
  ; @return (*)
  [query]
  (if (-> query map?)
      (-> query)
      (-> messages/QUERY-TYPE-ERROR Exception. throw)))


(ns monger.stages.preparing
    (:require [fruits.vector.api :as vector]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn prepare-document
  ; @description
  ; Applies the given 'prepare-f' function (if any) on the given document.
  ;
  ; @param (map) document
  ; @param (map)(opt) options
  ; {:prepare-f (function)(opt)}
  ;
  ; @usage
  ; (prepare-document {...}
  ;                   {:prepare-f (fn [%] (assoc % :my-namespace/my-key "My value"))})
  ; =>
  ; {:my-namespace/my-key "My value" ...}
  ;
  ; @return (map)
  ([document]
   (prepare-document document {}))

  ([document {:keys [prepare-f]}]
   (if (-> document some?)
       (if prepare-f (-> document prepare-f)
                     (-> document)))))

(defn prepare-documents
  ; @description
  ; Applies the 'prepare-document' function on the given documents.
  ;
  ; @param (maps in vector) documents
  ; @param (map)(opt) options
  ; {:prepare-f (function)(opt)}
  ;
  ; @usage
  ; (prepare-documents [{...}]
  ;                    {:prepare-f (fn [%] (assoc % :my-namespace/my-key "My value"))})
  ; =>
  ; [{:my-namespace/my-key "My value" ...}]
  ;
  ; @return (maps in vector)
  ([documents]
   (prepare-documents documents {}))

  ([documents options]
   (letfn [(f0 [%] (prepare-document % options))]
          (if (-> documents some?)
              (-> documents (vector/->items f0))))))


(ns monger.stages.prototyping
    (:require [fruits.vector.api :as vector]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn prototype-document
  ; @description
  ; Applies the given 'prototype-f' function (if any) on the given document.
  ;
  ; @param (map) document
  ; @param (map)(opt) options
  ; {:prototype-f (function)(opt)}
  ;
  ; @usage
  ; (prototype-document {...}
  ;                     {:prototype-f (fn [%] (assoc % :my-namespace/my-key "My value"))})
  ; =>
  ; {:my-namespace/my-key "My value" ...}
  ;
  ; @return (map)
  ([document]
   (prototype-document document {}))

  ([document {:keys [prototype-f]}]
   (if (-> document some?)
       (if prototype-f (-> document prototype-f)
                       (-> document)))))

(defn prototype-documents
  ; @description
  ; Applies the 'prototype-document' function on the given documents.
  ;
  ; @param (maps in vector) documents
  ; @param (map)(opt) options
  ; {:prototype-f (function)(opt)}
  ;
  ; @usage
  ; (prototype-documents [{...}]
  ;                      {:prototype-f (fn [%] (assoc % :my-namespace/my-key "My value"))})
  ; =>
  ; [{:my-namespace/my-key "My value" ...}]
  ;
  ; @return (maps in vector)
  ([documents]
   (prototype-documents documents {}))

  ([documents options]
   (letfn [(f0 [%] (prototype-document % options))]
          (if (-> documents some?)
              (-> documents (vector/->items f0))))))

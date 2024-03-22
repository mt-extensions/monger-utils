
(ns monger.stages.postparing
    (:require [fruits.vector.api :as vector]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn postpare-document
  ; @description
  ; Applies the given 'postpare-f' function (if any) on the given document.
  ;
  ; @param (map) document
  ; @param (map)(opt) options
  ; {:postpare-f (function)(opt)}
  ;
  ; @usage
  ; (postpare-document {...}
  ;                    {:postpare-f (fn [%] (assoc % :my-namespace/my-key "My value"))})
  ; =>
  ; {:my-namespace/my-key "My value" ...}
  ;
  ; @return (map)
  ([document]
   (postpare-document document {}))

  ([document {:keys [postpare-f]}]
   (if (-> document some?)
       (if postpare-f (-> document postpare-f)
                      (-> document)))))

(defn postpare-documents
  ; @description
  ; Applies the 'postpare-document' function on the given documents.
  ;
  ; @param (maps in vector) documents
  ; @param (map)(opt) options
  ; {:postpare-f (function)(opt)}
  ;
  ; @usage
  ; (postpare-documents [{...}]
  ;                     {:postpare-f (fn [%] (assoc % :my-namespace/my-key "My value"))})
  ; =>
  ; [{:my-namespace/my-key "My value" ...}]
  ;
  ; @return (maps in vector)
  ([documents]
   (postpare-documents documents {}))

  ([documents options]
   (letfn [(f0 [%] (postpare-document % options))]
          (if (-> documents some?)
              (-> documents (vector/->items f0))))))

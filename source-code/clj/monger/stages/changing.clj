
(ns monger.stages.changing
    (:require [fruits.vector.api :as vector]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn apply-document-changes
  ; @description
  ; Merges the given changes (if any) onto to the given document.
  ;
  ; @param (map) document
  ; @param (map)(opt) options
  ; {:changes (map)(opt)}
  ;
  ; @usage
  ; (apply-document-changes {...}
  ;                         {:changes {:my-namespace/my-key "My changes"}})
  ; =>
  ; {:my-namespace/my-key "My changes" ...}
  ;
  ; @return (map)
  ([document]
   (apply-document-changes document {}))

  ([document {:keys [changes]}]
   (if (-> document some?)
       (if changes (-> document (merge changes))
                   (-> document)))))

(defn apply-documents-changes
  ; @description
  ; Applies the 'apply-document-changes' function on the given documents.
  ;
  ; @param (maps in vector) documents
  ; @param (map)(opt) options
  ; {:changes (map)(opt)}
  ;
  ; @usage
  ; (apply-documents-changes [{...}]
  ;                          {:changes {:my-namespace/my-key "My changes"}})
  ; =>
  ; [{:my-namespace/my-key "My changes" ...}]
  ;
  ; @return (maps in vector)
  ([documents]
   (apply-documents-changes documents {}))

  ([documents options]
   (letfn [(f0 [%] (apply-document-changes % options))]
          (if (-> documents some?)
              (-> documents (vector/->items f0))))))

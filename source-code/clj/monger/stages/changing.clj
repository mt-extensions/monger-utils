
(ns monger.stages.changing)

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn apply-document-changes
  ; @description
  ; Merges the given changes on the given document.
  ;
  ; @param (map) document
  ; @param (map) options
  ; {:changes (map)}
  ;
  ; @usage
  ; (apply-document-changes {:my-namespace/id "MyObjectId" ...}
  ;                         {:changes {:my-namespace/my-key "Some changes from the client-side ..."}})
  ; =>
  ; {:my-namespace/id "MyObjectId"
  ;  :my-namespace/my-key "Some changes from the client-side ..."
  ;  ...}
  ;
  ; @return (map)
  [document {:keys [changes]}]
  (merge document changes))

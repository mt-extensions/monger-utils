
(ns monger.stages.preparing
    (:require [fruits.gestures.api    :as gestures]
              [monger.stages.messages :as messages]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn assoc-document-copy-label
  ; @description
  ; Optionally associates a copy label to the given document based on the labels of other documents
  ; returned by the given 'get-documents-f' function.
  ;
  ; @param (map) document
  ; @param (map) options
  ; {:label-key (keyword)(opt)}
  ; @param (function) get-documents-f
  ;
  ; @usage
  ; (assoc-document-copy-label {:my-namespace/my-label "My label"}
  ;                            {:label-key :my-label}
  ;                            #(-> [{:my-namespace/my-label "My label #2"}
  ;                                  {:my-namespace/my-label "My label #3"}]))
  ; =>
  ; {:my-namespace/my-label "My label #4"}
  ;
  ; @return (map)
  [document {:keys [label-key]} get-documents-f]
  (if label-key (if-let [documents (get-documents-f)]
                        (let [document-label   (monger.utils/derive-label document label-key)
                              concurent-labels (vector/all-results documents #(monger.utils/derive-label % label-key))
                              copy-label       (gestures/item-label->copy-label document-label concurent-labels)]
                             (monger.utils/assoc-label label-key copy-label))
                        (-> messages/FAILED-TO-GET-DOCUMENTS-ERROR Exception. throw))
                (-> document)))


(ns monger.stages.labeling
    (:require [fruits.gestures.api    :as gestures]
              [fruits.vector.api :as vector]
              [monger.stages.messages :as messages]
              [monger.tools.api :as monger.tools]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn assoc-document-copy-marker
  ; @description
  ; Associates a copy marker to the label of the given document (if ':label-key' is provided),
  ; based on the labels of other documents returned by the given 'get-documents-f' function.
  ;
  ; @param (map) document
  ; @param (map)(opt) options
  ; {:label-key (keyword)(opt)}
  ; @param (function)(opt) get-documents-f
  ; Must return maps in a vector.
  ;
  ; @usage
  ; (assoc-document-copy-marker {:my-namespace/my-label "My label #1" ...}
  ;                             {:label-key :my-label}
  ;                             #(-> [{:my-namespace/my-label "My label #1" ...}
  ;                                   {:my-namespace/my-label "My label #2" ...}
  ;                                   {:my-namespace/my-label "My label #3" ...}]))
  ; =>
  ; {:my-namespace/my-label "My label #4" ...}
  ;
  ; @return (map)
  ([document]
   (assoc-document-copy-marker document {} (fn [] [])))

  ([document options]
   (assoc-document-copy-marker document options (fn [] [])))

  ([document {:keys [label-key]} get-documents-f]
   (if (-> document some?)
       (if label-key (if-let [documents (get-documents-f)]
                             (let [document-label   (monger.tools/get-value document label-key)
                                   concurent-labels (vector/all-results documents #(monger.tools/get-value % label-key))
                                   copy-label       (gestures/item-label->copy-label document-label concurent-labels)]
                                  (monger.tools/assoc-value document label-key copy-label))
                             (-> messages/FAILED-TO-GET-DOCUMENTS-ERROR Exception. throw))
                     (-> document)))))

(defn assoc-documents-copy-marker
  ; @description
  ; Applies the 'assoc-document-copy-marker' function on the given documents.
  ;
  ; @param (maps in vector) documents
  ; @param (map)(opt) options
  ; {:label-key (keyword)(opt)}
  ; @param (function)(opt) get-documents-f
  ; Must return maps in a vector.
  ;
  ; @usage
  ; (assoc-documents-copy-marker [{:my-namespace/my-label "My label #1" ...}
  ;                               {:my-namespace/my-label "My label #2" ...}]
  ;                              {:label-key :my-label}
  ;                              #(-> [{:my-namespace/my-label "My label #1" ...}
  ;                                    {:my-namespace/my-label "My label #2" ...}
  ;                                    {:my-namespace/my-label "My label #3" ...}]))
  ; =>
  ; [{:my-namespace/my-label "My label #4" ...}
  ;  {:my-namespace/my-label "My label #5" ...}]
  ;
  ; @return (maps in vector)
  ([documents]
   (assoc-documents-copy-marker documents {} (fn [] [])))

  ([documents options]
   (assoc-documents-copy-marker documents options (fn [] [])))

  ([documents {:keys [label-key] :as options} get-documents-f]
   (letfn [(f1 [result dex %] (vector/all-results result #(monger.tools/get-value % label-key)))
           (f0 [result dex %] (if (-> dex zero?) (assoc-document-copy-marker % options get-documents-f)
                                                 (assoc-document-copy-marker % options (fn [] (vector/concat-items (get-documents-f) (f1 result dex %))))))]
          (if (-> documents some?)
              (-> documents (vector/->items f0 {:provide-dex? true :provide-result? true}))))))

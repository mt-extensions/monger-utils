
(ns monger.stages.positioning
    (:require [fruits.vector.api      :as vector]
              [monger.stages.messages :as messages]
              [monger.tools.api       :as monger.tools]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn assoc-document-position
  ; @description
  ; Associates a virtual position value (if ':position-key' is provided) to the given document,
  ; based on the output of the given 'get-document-count-f' function.
  ;
  ; @param (map) document
  ; @param (map)(opt) options
  ; {:position-key (keyword)(opt)}
  ; @param (function)(opt) get-document-count-f
  ; Must return an integer.
  ;
  ; @usage
  ; (assoc-document-position {...}
  ;                          {:position-key :my-position}
  ;                          #(-> 420))
  ; =>
  ; {:my-namespace/my-position 421 ...}
  ;
  ; @return (map)
  ([document]
   (assoc-document-position document {} (fn [] 0)))

  ([document options]
   (assoc-document-position document options (fn [] 0)))

  ([document {:keys [position-key]} get-document-count-f]
   (if (-> document some?)
       (if position-key (if-let [document-count (get-document-count-f)]
                                (-> document (monger.tools/assoc-value position-key (inc document-count)))
                                (-> messages/FAILED-TO-COUNT-DOCUMENTS-ERROR Exception. throw))
                        (-> document)))))

(defn assoc-documents-position
  ; @description
  ; Applies the 'assoc-document-position' function on the given documents.
  ;
  ; @param (maps in vector) documents
  ; @param (map)(opt) options
  ; {:position-key (keyword)(opt)}
  ; @param (function)(opt) get-document-count-f
  ; Must return an integer.
  ;
  ; @usage
  ; (assoc-documents-position [{...} {...}]
  ;                           {:position-key :my-position}
  ;                           #(-> 420))
  ; =>
  ; [{:my-namespace/my-position 421 ...}
  ;  {:my-namespace/my-position 422 ...}]
  ;
  ; @return (maps in vector)
  ([documents]
   (assoc-documents-position documents {} (fn [] 0)))

  ([documents options]
   (assoc-documents-position documents options (fn [] 0)))

  ([documents options get-document-count-f]
   (letfn [(f0 [dex %] (if (-> dex zero?) (assoc-document-position % options get-document-count-f)
                                          (assoc-document-position % options (fn [] (+ (get-document-count-f) dex)))))]
          (if (-> documents some?)
              (-> documents (vector/->items f0 {:provide-dex? true}))))))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn shift-document-position
  ; @description
  ; - Increments the virtual position value within the given document (if ':position-key' is provided).
  ; - Throws an error if the virtual position value is NIL.
  ;
  ; @param (map) document
  ; @param (map)(opt) options
  ; {:position-key (keyword)(opt)}
  ;
  ; @usage
  ; (shift-document-position {:my-namespace/my-position 420 ...}
  ;                          {:position-key :my-position})
  ; =>
  ; {:my-namespace/my-position 421 ...}
  ;
  ; @return (map)
  ([document]
   (shift-document-position document {}))

  ([document {:keys [position-key]}]
   (if (-> document some?)
       (if position-key (if (-> document (monger.tools/get-value    position-key))
                            (-> document (monger.tools/update-value position-key inc))
                            (-> messages/MISSING-DOCUMENT-POSITION-ERROR Exception. throw))
                        (-> document)))))

(defn shift-documents-position
  ; @description
  ; Applies the 'shift-document-position' function on the given documents.
  ;
  ; @param (maps in vector) documents
  ; @param (map)(opt) options
  ; {:position-key (keyword)(opt)}
  ;
  ; @usage
  ; (shift-documents-position [{:my-namespace/my-position 420 ...}]
  ;                           {:position-key :my-position})
  ; =>
  ; [{:my-namespace/my-position 421 ...}]
  ;
  ; @return (maps in vector)
  ([documents]
   (shift-documents-position documents {}))

  ([documents options]
   (letfn [(f0 [%] (shift-document-position % options))]
          (if (-> documents some?)
              (-> documents (vector/->items f0))))))

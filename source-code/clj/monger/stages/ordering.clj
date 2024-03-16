
(ns monger.stages.ordering
    (:require [monger.stages.messages :as messages]
              [monger.utils.api :as monger.utils]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn assoc-document-order
  ; @description
  ; Optionally associates a document order value to the given document based on the output of the given 'get-document-count-f' function.
  ;
  ; @param (map) document
  ; {:my-namespace/order (integer)(opt)
  ;  ...}
  ; @param (map) options
  ; {:order-key (keyword)(opt)}
  ; @param (function) get-document-count-f
  ;
  ; @usage
  ; (assoc-document-order {...}
  ;                       {:order-key :my-order}
  ;                       #(-> 420))
  ; =>
  ; {:my-namespace/my-order 421
  ;  ...}
  ;
  ; @return (map)
  [document {:keys [order-key]} get-document-count-f]
  (if order-key (if-let [document-count (get-document-count-f)]
                        (-> document (monger.utils/assoc-order order-key (inc document-count)))
                        (-> messages/FAILED-TO-COUNT-COLLECTION-ERROR Exception. throw))
                (-> document)))

(defn shift-document-order
  ; @description
  ; Optionally increases the order value within the given document or throws an error if missing.
  ;
  ; @param (map) document
  ; @param (map) options
  ; {:order-key (keyword)(opt)}
  ;
  ; @usage
  ; (shift-document-order {:my-namespace/my-order 420 ...}
  ;                       {:order-key :my-order})
  ; =>
  ; {:my-namespace/my-order 421
  ;  ...}
  ;
  ; @return (map)
  [document {:keys [order-key]}]
  (if order-key (if (-> document (monger.utils/derive-order order-key))
                    (-> document (monger.utils/shift-order  order-key))
                    (-> messages/MISSING-DOCUMENT-ORDER-ERROR Exception. throw))
                (-> document)))

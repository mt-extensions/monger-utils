
(ns monger.utils.order
    (:require [fruits.keyword.api     :as keyword]
              [fruits.map.api         :as map]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn derive-order
  ; @description
  ; Derives the order value from the given map.
  ;
  ; @param (map) n
  ; @param (keyword) order-key
  ;
  ; @usage
  ; (derive-order {:my-namespace/my-order 420 ...} :my-order)
  ; =>
  ; 420
  ;
  ; @return (integer)
  [n order-key]
  (if-let [namespace (map/namespace n)]
          (let [order-key (keyword/add-namespace order-key namespace)] (get n order-key))
          (let [order-key order-key]                                   (get n order-key))))

(defn assoc-order
  ; @description
  ; Associates the given order value to the given map.
  ;
  ; @param (map) n
  ; @param (keyword) order-key
  ; @param (integer) order
  ;
  ; @usage
  ; (assoc-order {:my-namespace/my-key "My value" ...} :my-order 420)
  ; =>
  ; {:my-namespace/my-key   "My value"
  ;  :my-namespace/my-order 420
  ;  ...}
  ;
  ; @return (map)
  [n order-key order]
  (if-let [namespace (map/namespace n)]
          (let [order-key (keyword/add-namespace order-key namespace)] (assoc n order-key order))
          (let [order-key order-key]                                   (assoc n order-key order))))

(defn shift-order
  ; @description
  ; Increases the order value (if any) within the given map.
  ;
  ; @param (map) n
  ; @param (keyword) order-key
  ;
  ; @usage
  ; (shift-order {:my-namespace/my-order 420 ...} :my-order)
  ; =>
  ; {:my-namespace/my-order 421 ...}
  ;
  ; @return (map)
  [n order-key]
  (if-let [namespace (map/namespace n)]
          (let [order-key (keyword/add-namespace order-key namespace)] (map/update-some n order-key inc))
          (let [order-key order-key]                                   (map/update-some n order-key inc))))

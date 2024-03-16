
(ns monger.utils.order
    (:require [fruits.keyword.api     :as keyword]
              [fruits.map.api         :as map]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn derive-label
  ; @description
  ; Derives the label value from the given map.
  ;
  ; @param (map) n
  ; @param (keyword) label-key
  ;
  ; @usage
  ; (derive-label {:my-namespace/my-label "My label" ...} :my-label)
  ; =>
  ; "My label"
  ;
  ; @return (*)
  [n label-key]
  (if-let [namespace (map/namespace n)]
          (let [label-key (keyword/add-namespace label-key namespace)] (get n label-key))
          (let [label-key label-key]                                   (get n label-key))))

(defn assoc-label
  ; @description
  ; Associates the given label value to the given map.
  ;
  ; @param (map) n
  ; @param (keyword) label-key
  ; @param (*) label
  ;
  ; @usage
  ; (assoc-label {:my-namespace/my-key "My value" ...} :my-label 420)
  ; =>
  ; {:my-namespace/my-key   "My value"
  ;  :my-namespace/my-label "My label"
  ;  ...}
  ;
  ; @return (map)
  [n label-key label]
  (if-let [namespace (map/namespace n)]
          (let [label-key (keyword/add-namespace label-key namespace)] (assoc n label-key label))
          (let [label-key label-key]                                   (assoc n label-key label))))

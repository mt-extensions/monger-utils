
(ns monger.tools.value
    (:require [fruits.map.api :as map]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn get-value
  ; @description
  ; Returns a specific value from the given optionally namespaced map.
  ;
  ; @param (map) n
  ; @param (keyword) k
  ;
  ; @usage
  ; (get-value {:my-namespace/my-key "My value" ...} :my-key)
  ; =>
  ; "My value"
  ;
  ; @return (*)
  [n k]
  (let [k (map/specify-key n k)]
       (get n k)))

(defn assoc-value
  ; @description
  ; Associates a specific value to the given optionally namespaced map.
  ;
  ; @param (map) n
  ; @param (keyword) k
  ; @param (*) v
  ;
  ; @usage
  ; (assoc-value {:my-namespace/my-key "My value" ...} :another-key "Another value")
  ; =>
  ; {:my-namespace/my-key      "My value"
  ;  :my-namespace/another-key "Another value"
  ;  ...}
  ;
  ; @return (map)
  [n k v]
  (let [k (map/specify-key n k)]
       (assoc n k v)))

(defn dissoc-value
  ; @description
  ; Dissociates a specific value of the given optionally namespaced map.
  ;
  ; @param (map) n
  ; @param (keyword) k
  ;
  ; @usage
  ; (dissoc-value {:my-namespace/my-key "My value" ...} :my-key)
  ; =>
  ; {...}
  ;
  ; @return (map)
  [n k]
  (let [k (map/specify-key n k)]
       (dissoc n k)))

(defn update-value
  ; @description
  ; Updates a specific value within the given optionally namespaced map.
  ;
  ; @param (map) n
  ; @param (keyword) k
  ; @param (function) f
  ; @param (list of *) params
  ;
  ; @usage
  ; (update-value {:my-namespace/my-key "My value" ...} :my-key clojure.string/upper-case)
  ; =>
  ; {:my-namespace/my-key "MY VALUE"
  ;  ...}
  ;
  ; @return (map)
  [n k f & params]
  (let [k (map/specify-key n k)]
       (apply update n k f params)))

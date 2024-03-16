
(ns monger.utils.walk)

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn walk
  ; @description
  ; Applies the given function on the given map and also on every nested map within.
  ;
  ; @param (map) n
  ; @param (function) f
  ;
  ; @usage
  ; (walk {:$or [{:my-namespace/id "MyObjectId"}] ...} adapt-id)
  ; =>
  ; {:$or [{:_id "MyObjectId"}]
  ;  ...}
  ;
  ; @return (map)
  [n f]
  (cond (map?    n) (reduce-kv #(assoc %1 %2 (walk %3)) {} (f n))
        (vector? n) (reduce    #(conj  %1    (walk %2)) [] n)
        :return  n))

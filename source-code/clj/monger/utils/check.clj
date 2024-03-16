
(ns monger.utils.check
    (:require [fruits.string.api :as string]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn operator?
  ; @description
  ; Checks whether the given value is a keyword representing a MongoDB operator.
  ;
  ; @param (*) n
  ;
  ; @usage
  ; (operator? :$or)
  ; =>
  ; true
  ;
  ; @return (boolean)
  [n]
  (and (-> n keyword?)
       (-> n string/second-character (= "$"))))

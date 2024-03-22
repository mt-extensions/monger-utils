
(ns monger.tools.check
    (:import org.bson.types.ObjectId)
    (:require [fruits.string.api :as string]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn object-id?
  ; @description
  ; Returns TRUE if the given value is an ObjectId object.
  ;
  ; @param (*) n
  ;
  ; @usage
  ; (object-id? #<ObjectId MyObjectId>)
  ; =>
  ; true
  ;
  ; @return (boolean)
  [n]
  (instance? ObjectId n))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn operator?
  ; @description
  ; Returns TRUE if the given value is a keyword representing a MongoDB operator.
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

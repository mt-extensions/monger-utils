
(ns monger.utils.convert
    (:require [monger.conversion :as mcv]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn DBObject->edn
  ; @description
  ; Converts the given MongoDB database object (DBObject) into an EDN map.
  ;
  ; @param (DBObject) n
  ;
  ; @usage
  ; (DBObject->edn ?)
  ; =>
  ; ?
  ;
  ; @return (map)
  [n]
  (mcv/from-db-object n true))

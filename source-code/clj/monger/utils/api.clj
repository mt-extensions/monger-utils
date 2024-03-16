
(ns monger.utils.api
    (:require [monger.utils.check :as check]
              [monger.utils.convert :as convert]
              [monger.utils.id :as id]
              [monger.utils.query :as query]
              [monger.utils.walk :as walk]
              [monger.utils.order :as order]
              [monger.utils.label :as label]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

; @redirect (monger.utils.check/*)
(def operator? check/operator?)

; @redirect (monger.utils.convert/*)
(def DBObject->edn convert/DBObject->edn)

; @redirect (monger.utils.id/*)
(def generate-id  id/generate-id)
(def assoc-id     id/assoc-id)
(def dissoc-id    id/dissoc-id)
(def parse-id     id/parse-id)
(def unparse-id   id/unparse-id)
(def adapt-id     id/adapt-id)
(def normalize-id id/normalize-id)

; @redirect (monger.utils.label/*)
(def derive-label label/derive-label)
(def assoc-label  label/assoc-label)

; @redirect (monger.utils.order/*)
(def derive-order order/derive-order)
(def assoc-order  order/assoc-order)
(def shift-order  order/shift-order)

; @redirect (monger.utils.query/*)
(def apply-dot-notation query/apply-dot-notation)
(def query<-namespace   query/query<-namespace)

; @redirect (monger.utils.walk/*)
(def walk walk/walk)

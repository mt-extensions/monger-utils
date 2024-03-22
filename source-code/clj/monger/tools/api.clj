
(ns monger.tools.api
    (:require [monger.tools.check :as check]
              [monger.tools.id    :as id]
              [monger.tools.query :as query]
              [monger.tools.value :as value]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

; @redirect (monger.tools.check/*)
(def object-id? check/object-id?)
(def operator?  check/operator?)

; @redirect (monger.tools.id/*)
(def generate-id id/generate-id)

; @redirect (monger.tools.query/*)
(def apply-dot-notation query/apply-dot-notation)
(def query<-namespace   query/query<-namespace)

; @redirect (monger.tools.value/*)
(def get-value    value/get-value)
(def assoc-value  value/assoc-value)
(def dissoc-value value/dissoc-value)
(def update-value value/update-value)

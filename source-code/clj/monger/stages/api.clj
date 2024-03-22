
(ns monger.stages.api
    (:require [monger.stages.acknowledging :as acknowledging]
              [monger.stages.adaptation    :as adaptation]
              [monger.stages.changing      :as changing]
              [monger.stages.checking      :as checking]
              [monger.stages.id            :as id]
              [monger.stages.labeling      :as labeling]
              [monger.stages.normalization :as normalization]
              [monger.stages.positioning   :as positioning]
              [monger.stages.postparing    :as postparing]
              [monger.stages.preparing     :as preparing]
              [monger.stages.prototyping   :as prototyping]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

; @tutorial Demo
;
; @usage
; (let [document {:my-namespace/my-id "MyObjectId" :my-namespace/my-key "My value" ...}
;       result   (monger.collection/insert-and-return MY-DB (-> document (prototype-document {:prototype-f (fn [%] ...)})
;                                                                        (prepare-document   {:prepare-f   (fn [%] ...)})
;                                                                        (postpare-document  {:postpare-f  (fn [%] ...)})
;                                                                        (adapt-document-id  {:id-key :my-id)
;                                                                        (adapt-document     {:parse-id? true :unkeywordize-keys? true :unkeywordize-values? true)
;                                                                        (check-document     {:check-ns? true)))]
;      (-> result (normalize-document    {:keywordize-keys? true :keywordize-values? true :unparse-id? true})
;                 (normalize-document-id {:id-key :my-id})))])
; =>
; {:my-namespace/my-id "MyObjectId" :my-namespace/my-key "My value" ...}

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

; @redirect (monger.stages.acknowledging/*)
(def result-acknowledged?  acknowledging/result-acknowledged?)
(def results-acknowledged? acknowledging/results-acknowledged?)
(def updated-existing?     acknowledging/updated-existing?)
(def updated-existings?    acknowledging/updated-existings?)

; @redirect (monger.stages.adaptation/*)
(def adapt-database-name         adaptation/adapt-database-name)
(def adapt-databases-name        adaptation/adapt-databases-name)
(def adapt-collection-name       adaptation/adapt-collection-name)
(def adapt-collections-name      adaptation/adapt-collections-name)
(def adapt-collection-namespace  adaptation/adapt-collection-namespace)
(def adapt-collections-namespace adaptation/adapt-collections-namespace)
(def adapt-locale                adaptation/adapt-locale)
(def adapt-locales               adaptation/adapt-locales)
(def parse-id                    adaptation/parse-id)
(def parse-ids                   adaptation/parse-ids)
(def adapt-document-id           adaptation/adapt-document-id)
(def adapt-documents-id          adaptation/adapt-documents-id)
(def adapt-document              adaptation/adapt-document)
(def adapt-documents             adaptation/adapt-documents)
(def adapt-projection-id         adaptation/adapt-projection-id)
(def adapt-projections-id        adaptation/adapt-projections-id)
(def adapt-projection            adaptation/adapt-projection)
(def adapt-projections           adaptation/adapt-projections)
(def adapt-query-id              adaptation/adapt-query-id)
(def adapt-queries-id            adaptation/adapt-queries-id)
(def adapt-query                 adaptation/adapt-query)
(def adapt-queries               adaptation/adapt-queries)
(def adapt-pipeline-stage-id     adaptation/adapt-pipeline-stage-id)
(def adapt-pipeline-stages-id    adaptation/adapt-pipeline-stages-id)
(def adapt-pipeline-stage        adaptation/adapt-pipeline-stage)
(def adapt-pipeline-stages       adaptation/adapt-pipeline-stages)

; @redirect (monger.stages.changing/*)
(def apply-document-changes  changing/apply-document-changes)
(def apply-documents-changes changing/apply-documents-changes)

; @redirect (monger.stages.checking/*)
(def check-database-name         checking/check-database-name)
(def check-databases-name        checking/check-databases-name)
(def check-collection-name       checking/check-collection-name)
(def check-collections-name      checking/check-collections-name)
(def check-collection-namespace  checking/check-collection-namespace)
(def check-collections-namespace checking/check-collections-namespace)
(def check-locale                checking/check-locale)
(def check-locales               checking/check-locales)
(def check-id                    checking/check-id)
(def check-ids                   checking/check-ids)
(def check-document              checking/check-document)
(def check-documents             checking/check-documents)
(def check-projection            checking/check-projection)
(def check-projections           checking/check-projections)
(def check-query                 checking/check-query)
(def check-queries               checking/check-queries)
(def check-pipeline-stage        checking/check-pipeline-stage)
(def check-pipeline-stages       checking/check-pipeline-stages)

; @redirect (monger.stages.id/*)
(def assoc-document-id   id/assoc-document-id)
(def assoc-documents-id  id/assoc-documents-id)
(def dissoc-document-id  id/dissoc-document-id)
(def dissoc-documents-id id/dissoc-documents-id)

; @redirect (monger.stages.labeling/*)
(def assoc-document-copy-marker  labeling/assoc-document-copy-marker)
(def assoc-documents-copy-marker labeling/assoc-documents-copy-marker)

; @redirect (monger.stages.normalization/*)
(def normalize-database-name         normalization/normalize-database-name)
(def normalize-databases-name        normalization/normalize-databases-name)
(def normalize-collection-name       normalization/normalize-collection-name)
(def normalize-collections-name      normalization/normalize-collections-name)
(def normalize-collection-namespace  normalization/normalize-collection-namespace)
(def normalize-collections-namespace normalization/normalize-collections-namespace)
(def normalize-locale                normalization/normalize-locale)
(def normalize-locales               normalization/normalize-locales)
(def unparse-id                      normalization/unparse-id)
(def unparse-ids                     normalization/unparse-ids)
(def normalize-document-id           normalization/normalize-document-id)
(def normalize-documents-id          normalization/normalize-documents-id)
(def normalize-document              normalization/normalize-document)
(def normalize-documents             normalization/normalize-documents)

; @redirect (monger.stages.positioning/*)
(def assoc-document-position  positioning/assoc-document-position)
(def assoc-documents-position positioning/assoc-documents-position)
(def shift-document-position  positioning/shift-document-position)
(def shift-documents-position positioning/shift-documents-position)

; @redirect (monger.stages.postparing/*)
(def postpare-document  postparing/postpare-document)
(def postpare-documents postparing/postpare-documents)

; @redirect (monger.stages.preparing/*)
(def prepare-document  preparing/prepare-document)
(def prepare-documents preparing/prepare-documents)

; @redirect (monger.stages.prototyping/*)
(def prototype-document  prototyping/prototype-document)
(def prototype-documents prototyping/prototype-documents)

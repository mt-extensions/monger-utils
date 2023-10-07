
(ns mongo-db.api
    (:require [mongo-db.actions.engine          :as actions.engine]
              [mongo-db.aggregation.pipelines   :as aggregation.pipelines]
              [mongo-db.connection.env          :as connection.env]
              [mongo-db.connection.side-effects :as connection.side-effects]
              [mongo-db.core.utils              :as core.utils]
              [mongo-db.reader.engine           :as reader.engine]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

; mongo-db.actions.engine
(def insert-document!      actions.engine/insert-document!)
(def insert-documents!     actions.engine/insert-documents!)
(def save-document!        actions.engine/save-document!)
(def save-documents!       actions.engine/save-documents!)
(def update-document!      actions.engine/update-document!)
(def update-documents!     actions.engine/update-documents!)
(def upsert-document!      actions.engine/upsert-document!)
(def upsert-documents!     actions.engine/upsert-documents!)
(def apply-on-document!    actions.engine/apply-on-document!)
(def apply-on-collection!  actions.engine/apply-on-collection!)
(def remove-document!      actions.engine/remove-document!)
(def remove-documents!     actions.engine/remove-documents!)
(def remove-all-documents! actions.engine/remove-all-documents!)
(def duplicate-document!   actions.engine/duplicate-document!)
(def duplicate-documents!  actions.engine/duplicate-documents!)
(def reorder-documents!    actions.engine/reorder-documents!)

; mongo-db.aggregation.pipelines
(def get-pipeline   aggregation.pipelines/get-pipeline)
(def count-pipeline aggregation.pipelines/count-pipeline)

; mongo-db.connection.env
(def connected? connection.env/connected?)

; mongo-db.connection.side-effects
(def build-connection! connection.side-effects/build-connection!)

; mongo-db.core.utils
(def generate-id      core.utils/generate-id)
(def query<-namespace core.utils/query<-namespace)
(def flatten-query    core.utils/flatten-query)

; mongo-db.reader.engine
(def get-collection-names        reader.engine/get-collection-names)
(def get-collection-namespace    reader.engine/get-collection-namespace)
(def get-all-document-count      reader.engine/get-all-document-count)
(def collection-empty?           reader.engine/collection-empty?)
(def get-document-count-by-query reader.engine/get-document-count-by-query)
(def get-collection              reader.engine/get-collection)
(def get-documents-by-query      reader.engine/get-documents-by-query)
(def get-document-by-query       reader.engine/get-document-by-query)
(def get-document-by-id          reader.engine/get-document-by-id)
(def get-first-document          reader.engine/get-first-document)
(def get-last-document           reader.engine/get-last-document)
(def document-exists?            reader.engine/document-exists?)
(def count-documents-by-pipeline reader.engine/count-documents-by-pipeline)
(def get-documents-by-pipeline   reader.engine/get-documents-by-pipeline)
(def get-specified-values        reader.engine/get-specified-values)

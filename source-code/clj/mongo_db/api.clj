
;; -- Namespace ---------------------------------------------------------------
;; ----------------------------------------------------------------------------

(ns mongo-db.api
    (:require [mongo-db.connection]
              [mongo-db.actions   :as actions]
              [mongo-db.engine    :as engine]
              [mongo-db.pipelines :as pipelines]
              [mongo-db.reader    :as reader]))



;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

; mongo-db.actions
(def insert-document!      actions/insert-document!)
(def insert-documents!     actions/insert-documents!)
(def save-document!        actions/save-document!)
(def save-documents!       actions/save-documents!)
(def update-document!      actions/update-document!)
(def update-documents!     actions/update-documents!)
(def upsert-document!      actions/upsert-document!)
(def upsert-documents!     actions/upsert-documents!)
(def apply-document!       actions/apply-document!)
(def apply-documents!      actions/apply-documents!)
(def remove-document!      actions/remove-document!)
(def remove-documents!     actions/remove-documents!)
(def remove-all-documents! actions/remove-all-documents!)
(def duplicate-document!   actions/duplicate-document!)
(def duplicate-documents!  actions/duplicate-documents!)
(def reorder-documents!    actions/reorder-documents!)

; mongo-db.engine
(def generate-id engine/generate-id)

; mongo-db.pipelines
(def add-fields-query pipelines/add-fields-query)
(def filter-query     pipelines/filter-query)
(def search-query     pipelines/search-query)
(def sort-query       pipelines/sort-query)
(def unset-query      pipelines/unset-query)
(def get-pipeline     pipelines/get-pipeline)
(def count-pipeline   pipelines/count-pipeline)

; mongo-db.reader
(def get-collection-names        reader/get-collection-names)
(def get-collection-namespace    reader/get-collection-namespace)
(def get-all-document-count      reader/get-all-document-count)
(def collection-empty?           reader/collection-empty?)
(def get-document-count-by-query reader/get-document-count-by-query)
(def get-collection              reader/get-collection)
(def get-documents-by-query      reader/get-documents-by-query)
(def get-document-by-query       reader/get-document-by-query)
(def get-document-by-id          reader/get-document-by-id)
(def get-first-document          reader/get-first-document)
(def get-last-document           reader/get-last-document)
(def document-exists?            reader/document-exists?)
(def count-documents-by-pipeline reader/count-documents-by-pipeline)
(def get-documents-by-pipeline   reader/get-documents-by-pipeline)
(def get-specified-values        reader/get-specified-values)

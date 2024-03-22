
(ns monger.stages.messages)

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

; @ignore
;
; @constant (string)
(def DATABASE-NAME-TYPE-ERROR "Database name must be a keyword.")

; @ignore
;
; @constant (string)
(def COLLECTION-NAME-TYPE-ERROR "Collection name must be a keyword.")

; @ignore
;
; @constant (string)
(def COLLECTION-NAMESPACE-TYPE-ERROR "Collection namespace must be a keyword.")

; @ignore
;
; @constant (string)
(def LOCALE-TYPE-ERROR "Locale must be a keyword.")

; @ignore
;
; @constant (string)
(def ID-TYPE-ERROR "ID must be an ObjectId object.")

; @ignore
;
; @constant (string)
(def DOCUMENT-EMPTY-ERROR "Document must be a nonempty map.")

; @ignore
;
; @constant (string)
(def DOCUMENT-TYPE-ERROR "Document must be a map.")

; @ignore
;
; @constant (string)
(def NAMESPACED-DOCUMENT-TYPE-ERROR "Document must be a namespaced map.")

; @ignore
;
; @constant (string)
(def PROJECTION-EMPTY-ERROR "Projection must be a nonempty map.")

; @ignore
;
; @constant (string)
(def PROJECTION-TYPE-ERROR "Projection must be a map.")

; @ignore
;
; @constant (string)
(def NAMESPACED-PROJECTION-TYPE-ERROR "Projection must be a namespaced map.")

; @ignore
;
; @constant (string)
(def QUERY-EMPTY-ERROR "Query must be a nonempty map.")

; @ignore
;
; @constant (string)
(def QUERY-TYPE-ERROR "Query must be a map.")

; @ignore
;
; @constant (string)
(def NAMESPACED-QUERY-TYPE-ERROR "Query must be a namespaced map.")

; @ignore
;
; @constant (string)
(def PIPILINE-STAGE-EMPTY-ERROR "Pipeline stage must be a nonempty map.")

; @ignore
;
; @constant (string)
(def PIPILINE-STAGE-TYPE-ERROR "Pipeline stage must be a map.")

; @ignore
;
; @constant (string)
(def NAMESPACED-PIPILINE-STAGE-TYPE-ERROR "Pipeline stage must be a namespaced map.")

; @ignore
;
; @constant (string)
(def MISSING-DOCUMENT-POSITION-ERROR "Missing document position.")

; @ignore
;
; @constant (string)
(def FAILED-TO-COUNT-DOCUMENTS-ERROR "Failed to count documents.")

; @ignore
;
; @constant (string)
(def FAILED-TO-GET-DOCUMENTS-ERROR "Failed to get documents.")

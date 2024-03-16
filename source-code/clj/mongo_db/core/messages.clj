
(ns mongo-db.core.messages)

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

; @ignore
;
; @constant (string)
(def QUERY-TYPE-ERROR "Query must be a map.")

; @ignore
;
; @constant (string)
(def DOCUMENT-TYPE-ERROR "Document must be a map.")

; @ignore
;
; @constant (string)
(def NAMESPACED-DOCUMENT-TYPE-ERROR "Document must be a namespaced map.")

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

; @ignore
;
; @constant (string)
(def MISSING-DOCUMENT-ID-ERROR "Missing document ID.")

; @ignore
;
; @constant (string)
(def MISSING-DOCUMENT-ORDER-ERROR "Missing document order.")

; @ignore
;
; @constant (string)
(def DOCUMENT-DOES-NOT-EXIST-ERROR "Document does not exist.")

; @ignore
;
; @constant (string)
(def DOCUMENT-CORRUPTED-ERROR "Document corrupted.")

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

; @ignore
;
; @constant (string)
(def FAILED-TO-REORDER-DOCUMENTS-ERROR "Failed to reorder documents.")

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

; @ignore
;
; @constant (string)
(def MISSING-DATABASE-NAME-AND-NO-CONNECTION-ERROR "Missing database name. Cannot assume a default database name if no database connection is estabilished.")

; @ignore
;
; @constant (string)
(def MISSING-DATABASE-NAME-AND-MULTIPLE-CONNECTIONS-ERROR "Missing database name. Cannot assume a default database name if multiple database connections are estabilished.")

; @ignore
;
; @constant (string)
(def NO-DATABASE-REFERENCE-FOUND-ERROR "No connected database reference is found.")

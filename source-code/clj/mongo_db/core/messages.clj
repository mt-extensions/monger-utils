
(ns mongo-db.core.messages)

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

; @ignore
;
; @constant (string)
(def QUERY-MUST-BE-MAP-ERROR "Query must be map")

; @ignore
;
; @constant (string)
(def INPUT-MUST-BE-MAP-ERROR "Input must be map")

; @ignore
;
; @constant (string)
(def MISSING-NAMESPACE-ERROR "Document must be a namespaced map with keyword type keys")

; @ignore
;
; @constant (string)
(def MISSING-DOCUMENT-ID-ERROR "Missing document ID error")

; @ignore
;
; @constant (string)
(def MISSING-DOCUMENT-ORDER-ERROR "Missing document order error")

; @ignore
;
; @constant (string)
(def DOCUMENT-DOES-NOT-EXISTS-ERROR "Document does not exists error")

; @ignore
;
; @constant (string)
(def DOCUMENT-CORRUPTED-ERROR "Document corrupted error")

; @ignore
;
; @constant (string)
(def REORDERING-DOCUMENTS-FAILED "Reordering documents has been failed")

; @ignore
;
; @constant (string)
(def REMOVING-DOCUMENT-FAILED "Removing document has been failed")

; @ignore
;
; @constant (string)
(def APPLYING-FUNCTION-FAILED "Applying function has been failed")

; @ignore
;
; @constant (string)
(def MISSING-DATABASE-NAME-AND-NO-CONNECTION-ERROR "Missing database name and cannot assume default database name if no database connection is estabilished")

; @ignore
;
; @constant (string)
(def MISSING-DATABASE-NAME-AND-MULTI-CONNECTION-ERROR "Missing database name and cannot assume default database name if multiple database connections are estabilished")

; @ignore
;
; @constant (string)
(def NO-DATABASE-REFERENCE-FOUND-ERROR "No connected database reference is found")

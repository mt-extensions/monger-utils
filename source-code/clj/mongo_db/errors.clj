
;; -- Namespace ---------------------------------------------------------------
;; ----------------------------------------------------------------------------

(ns mongo-db.errors)



;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

; @constant (string)
(def QUERY-MUST-BE-MAP-ERROR "Query must be map")

; @constant (string)
(def INPUT-MUST-BE-MAP-ERROR "Input must be map")

; @constant (string)
(def MISSING-NAMESPACE-ERROR "Document must be a namespaced map with keyword type keys")

; @constant (string)
(def MISSING-DOCUMENT-ID-ERROR "Missing document ID error")

; @constant (string)
(def MISSING-DOCUMENT-ORDER-ERROR "Missing document order error")

; @constant (string)
(def DOCUMENT-DOES-NOT-EXISTS-ERROR "Document does not exists error")

; @constant (string)
(def DOCUMENT-CORRUPTED-ERROR "Document corrupted error")

; @constant (string)
(def REORDER-DOCUMENTS-FAILED "Reordering documents failed")

; @constant (string)
(def REMOVING-DOCUMENT-FAILED "Removing document failed")

; @constant (string)
(def APPLYING-FUNCTION-FAILED "Applying function failed")

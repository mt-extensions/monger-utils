
(ns mongo-db.connection.state)

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

; @ignore
;
; @atom (map)
; {"my-database" (com.mongodb.DB object)}
(def REFERENCES (atom {}))


(ns mongo-db.connection.utils
    (:require [fruits.string.api :as string]
              [mongo-db.core.messages :as core.messages]
              [mongo-db.connection.env :as connection.env]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn collection-path->database-name
  ; @ignore
  ;
  ; @description
  ; Derives the database name from the 'collection-path' before the '/' character.
  ; If no database name found in the 'collection-path' and only one database connection
  ; estabilished, returns the only connected database name.
  ;
  ; @usage
  ; (collection-path->database-name "my_collection")
  ;
  ; @usage
  ; (collection-path->database-name "my-database/my_collection")
  ;
  ; @example
  ; (collection-path->database-name "my-database/my_collection")
  ; =>
  ; "my-database"
  ;
  ; @example
  ; (collection-path->database-name "my_collection")
  ; =>
  ; "the-only-connected-database-name"
  ;
  ; @return (string)
  [collection-path]
  (if-let [database-name (string/before-first-occurence collection-path "/" {:return? false})]
          (-> database-name)
          (connection.env/get-default-database-name)))

(defn collection-path->collection-name
  ; @ignore
  ;
  ; @usage
  ; (collection-path->collection-name "my_collection")
  ;
  ; @usage
  ; (collection-path->collection-name "my-database/my_collection")
  ;
  ; @example
  ; (collection-path->collection-name "my_collection")
  ; =>
  ; "my_collection"
  ;
  ; @example
  ; (collection-path->collection-name "my-database/my_collection")
  ; =>
  ; "my_collection"
  ;
  ; @return (string)
  [collection-path]
  (string/after-first-occurence collection-path "/" {:return? true}))


(ns mongo-db.connection.utils
    (:require [fruits.string.api         :as string]
              [mongo-db.connection.state :as connection.state]
              [mongo-db.core.messages      :as core.messages]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn default-database-name
  ; @ignore
  ;
  ; @description
  ; - Returns the connected database's name as default database name in case
  ;   of only one database is connected, otherwise it throws an error.
  ;
  ; @return (string)
  []
  (let [connection-count (-> @connection.state/REFERENCES keys count)]
       (case connection-count 1 (-> @connection.state/REFERENCES keys first)
                              0 (throw (Exception. core.messages/MISSING-DATABASE-NAME-AND-NO-CONNECTION-ERROR))
                                (throw (Exception. core.messages/MISSING-DATABASE-NAME-AND-MULTI-CONNECTION-ERROR)))))

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
          (default-database-name)))

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

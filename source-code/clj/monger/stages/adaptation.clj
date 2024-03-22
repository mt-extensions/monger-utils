
(ns monger.stages.adaptation
    (:import org.bson.types.ObjectId)
    (:require [fruits.bson.api :as bson]
              [fruits.json.api :as json]
              [fruits.map.api :as map]
              [fruits.vector.api :as vector]
              [fruits.syntax.api :as syntax]
              [time.api :as time]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn adapt-database-name
  ; @description
  ; Converts the given database name into a string (MongoDB compatible).
  ;
  ; @param (keyword) database-name
  ;
  ; @usage
  ; (adapt-database-name :my-database)
  ; =>
  ; "my-database"
  ;
  ; @return (string)
  [database-name]
  (if (-> database-name some?)
      (-> database-name name)))

(defn adapt-databases-name
  ; @description
  ; Applies the 'adapt-database-name' function on the given database names.
  ;
  ; @param (keywords in vector) database-names
  ;
  ; @usage
  ; (adapt-databases-name [:my-database :another-database])
  ; =>
  ; ["my-database" "another-database"]
  ;
  ; @return (strings in vector)
  [database-names]
  (if (-> database-names some?)
      (-> database-names (vector/->items adapt-database-name))))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn adapt-collection-name
  ; @description
  ; Converts the given collection name into a string in snake_case form (MongoDB compatible).
  ;
  ; @param (keyword) collection-name
  ;
  ; @usage
  ; (adapt-collection-name :my-collection)
  ; =>
  ; "my_collection"
  ;
  ; @return (string)
  [collection-name]
  (if (-> collection-name some?)
      (-> collection-name name syntax/to-snake_case)))

(defn adapt-collections-name
  ; @description
  ; Applies the 'adapt-collection-name' function on the given collection names.
  ;
  ; @param (keywords in vector) collection-names
  ;
  ; @usage
  ; (adapt-collections-name [:my-collection :another-collection])
  ; =>
  ; ["my_collection" "another_collection"]
  ;
  ; @return (strings in vector)
  [collection-names]
  (if (-> collection-names some?)
      (-> collection-names (vector/->items adapt-collection-name))))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn adapt-collection-namespace
  ; @description
  ; Converts the given collection namespace into a string (MongoDB compatible).
  ;
  ; @param (keyword) collection-namespace
  ;
  ; @usage
  ; (adapt-collection-namespace :my-namespace)
  ; =>
  ; "my-namespace"
  ;
  ; @return (string)
  [collection-namespace]
  (if (-> collection-namespace some?)
      (-> collection-namespace name)))

(defn adapt-collections-namespace
  ; @description
  ; Applies the 'adapt-collection-namespace' function on the given collection namespaces.
  ;
  ; @param (keywords in vector) collection-namespaces
  ;
  ; @usage
  ; (adapt-collections-namespace [:my-namespace :another-namespace])
  ; =>
  ; ["my-namespace" "another-namespace"]
  ;
  ; @return (strings in vector)
  [collection-namespaces]
  (if (-> collection-namespaces some?)
      (-> collection-namespaces (vector/->items adapt-collection-namespace))))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn adapt-locale
  ; @description
  ; Converts the given locale into a string (MongoDB compatible).
  ;
  ; @param (keyword) locale
  ;
  ; @usage
  ; (adapt-locale :my-locale)
  ; =>
  ; "my-locale"
  ;
  ; @return (string)
  [locale]
  (if (-> locale some?)
      (-> locale name)))

(defn adapt-locales
  ; @description
  ; Applies the 'adapt-locale' function on the given locales.
  ;
  ; @param (keywords in vector) locales
  ;
  ; @usage
  ; (adapt-locale [:my-locale :another-locale])
  ; =>
  ; ["my-locale" "another-locale"]
  ;
  ; @return (strings in vector)
  [locales]
  (if (-> locales some?)
      (-> locales (vector/->items adapt-locale))))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn parse-id
  ; @description
  ; Parses the given ObjectId string into an object.
  ;
  ; @param (string) id
  ;
  ; @usage
  ; (parse-id "MyObjectId")
  ; =>
  ; #<ObjectId MyObjectId>
  ;
  ; @return (org.bson.types.ObjectId object)
  [id]
  (if (-> id some?)
      (if (-> id string?)
          (-> id ObjectId.)
          (-> id))))

(defn parse-ids
  ; @description
  ; Applies the 'parse-id' function on the given ObjectId strings.
  ;
  ; @param (strings in vector) ids
  ;
  ; @usage
  ; (parse-ids ["MyObjectId" "AnotherObjectId"])
  ; =>
  ; [#<ObjectId MyObjectId> #<ObjectId AnotherObjectId>]
  ;
  ; @return (org.bson.types.ObjectId objects in vector)
  [ids]
  (if (-> ids some?)
      (-> ids (vector/->items parse-id))))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn adapt-document-id
  ; @description
  ; Renames the given ID key to ':_id' (MongoDB compatible) within the given document (if ':id-key' is provided).
  ;
  ; @param (map) document
  ; @param (map)(opt) options
  ; {:id-key (keyword)(opt)}
  ;
  ; @usage
  ; (adapt-document-id {:my-namespace/my-id "MyObjectId" ...}
  ;                    {:id-key :my-id})
  ; =>
  ; {:_id "MyObjectId" ...}
  ;
  ; @return (map)
  ([document]
   (adapt-document-id document {}))

  ([document {:keys [id-key]}]
   (if (-> document some?)
       (if id-key (-> document (map/move-some (map/specify-key document id-key) :_id))
                  (-> document)))))

(defn adapt-documents-id
  ; @description
  ; Applies the 'adapt-document-id' function on the given documents.
  ;
  ; @param (maps in vector) documents
  ; @param (map)(opt) options
  ; {:id-key (keyword)(opt)}
  ;
  ; @usage
  ; (adapt-documents-id [{:my-namespace/my-id "MyObjectId" ...}]
  ;                     {:id-key :my-id})
  ; =>
  ; [{:_id "MyObjectId" ...}]
  ;
  ; @return (maps in vector)
  ([documents]
   (adapt-documents-id documents {}))

  ([documents options]
   (letfn [(f0 [%] (adapt-document-id % options))]
          (if (-> documents some?)
              (-> documents (vector/->items f0))))))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn adapt-document
  ; @description
  ; 1. Optionally parses the document ID string into object within the given document.
  ; 2. Optionally parses date and time strings to objects within the given document.
  ; 3. Optionally removes '.' characters from keys within the given document to prevent them misread as dot notations (BSON syntax requirement).
  ; 4. Optionally converts keyword type keys and/or values into strings within the given document.
  ;
  ; @param (map) document
  ; @param (map)(opt) options
  ; {:parse-id? (boolean)(opt)
  ;  :parse-timestamps? (boolean)(opt)
  ;  :undot-keys? (boolean)(opt)
  ;  :unkeywordize-keys? (boolean)(opt)
  ;  :unkeywordize-values?? (boolean)(opt)}
  ;
  ; @usage
  ; (adapt-document {:_id                       "MyObjectId"
  ;                  :my-namespace/my-keyword   :my-value
  ;                  :my-namespace/my-string    "My value"
  ;                  :my-namespace/my-timestamp "2020-04-20T16:20:00.000Z" ...}
  ;                 {:parse-id? true :parse-timestamps? true :unkeywordize-keys? true :unkeywordize-values? true})
  ; =>
  ; {"_id"                       #<ObjectId MyObjectId>
  ;  "my-namespace/my-keyword"   "*:my-value"
  ;  "my-namespace/my-string"    "My value"
  ;  "my-namespace/my-timestamp" #<DateTime 2020-04-20T16:20:00.123Z> ...}
  ;
  ; @return (map)
  ([document]
   (adapt-document document {}))

  ([document {:keys [parse-id? parse-timestamps? undot-keys? unkeywordize-keys? unkeywordize-values?]}]
   (if (->     document some?)
       (cond-> document parse-id?            (map/update-some :_id parse-id)
                        parse-timestamps?    (time/parse-timestamps)
                        undot-keys?          (bson/undot-keys)
                        unkeywordize-keys?   (json/unkeywordize-keys)
                        unkeywordize-values? (json/unkeywordize-values)))))

(defn adapt-documents
  ; @description
  ; Applies the 'adapt-document' function on the given documents.
  ;
  ; @param (maps in vector) documents
  ; @param (map)(opt) options
  ; {:parse-id? (boolean)(opt)
  ;  :parse-timestamps? (boolean)(opt)
  ;  :undot-keys? (boolean)(opt)
  ;  :unkeywordize-keys? (boolean)(opt)
  ;  :unkeywordize-values?? (boolean)(opt)}
  ;
  ; @usage
  ; (adapt-documents [{:_id                       "MyObjectId"
  ;                    :my-namespace/my-keyword   :my-value
  ;                    :my-namespace/my-string    "My value"
  ;                    :my-namespace/my-timestamp "2020-04-20T16:20:00.000Z" ...}]
  ;                  {:parse-id? true :parse-timestamps? true :unkeywordize-keys? true :unkeywordize-values? true})
  ; =>
  ; [{"_id"                       #<ObjectId MyObjectId>
  ;   "my-namespace/my-keyword"   "*:my-value"
  ;   "my-namespace/my-string"    "My value"
  ;   "my-namespace/my-timestamp" #<DateTime 2020-04-20T16:20:00.123Z> ...}]
  ;
  ; @return (maps in vector)
  ([documents]
   (adapt-documents documents {}))

  ([documents options]
   (letfn [(f0 [%] (adapt-document % options))]
          (if (-> documents some?)
              (-> documents (vector/->items f0))))))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn adapt-projection-id
  ; @description
  ; Renames the given ID key to ':_id' (a MongoDB compatible identifier) within the given projection (if ':id-key' is provided).
  ;
  ; @param (map) projection
  ; @param (map)(opt) options
  ; {:id-key (keyword)(opt)}
  ;
  ; @usage
  ; (adapt-projection-id {:my-namespace/my-id -1 ...}
  ;                      {:id-key :my-id})
  ; =>
  ; {:_id -1 ...}
  ;
  ; @return (map)
  ([projection]
   (adapt-projection-id projection {}))

  ([projection {:keys [id-key]}]
   (if (-> projection some?)
       (if id-key (-> projection (map/move-some (map/specify-key projection id-key) :_id))
                  (-> projection)))))

(defn adapt-projections-id
  ; @description
  ; Applies the 'adapt-projection-id' function on the given projections.
  ;
  ; @param (maps in vector) projections
  ; @param (map)(opt) options
  ; {:id-key (keyword)(opt)}
  ;
  ; @usage
  ; (adapt-projections-id [{:my-namespace/my-id -1 ...}]
  ;                       {:id-key :my-id})
  ; =>
  ; [{:_id -1 ...}]
  ;
  ; @return (maps in vector)
  ([projections]
   (adapt-projections-id projections {}))

  ([projections options]
   (letfn [(f0 [%] (adapt-projection-id % options))]
          (if (-> projections some?)
              (-> projections (vector/->items f0))))))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn adapt-projection
  ; @description
  ; 1. Optionally removes '.' characters from keys within the given projection to prevent them misread as dot notations (BSON syntax requirement).
  ; 2. Optionally converts keyword type keys into strings within the given projection.
  ;
  ; @param (map) projection
  ; @param (map)(opt) options
  ; {:undot-keys? (boolean)(opt)
  ;  :unkeywordize-keys? (boolean)(opt)}
  ;
  ; @usage
  ; (adapt-projection {:_id                 -1
  ;                    :my-namespace/my-key -1 ...}
  ;                   {:unkeywordize-keys? true})
  ; =>
  ; {"_id"                 -1
  ;  "my-namespace/my-key" -1 ...}
  ;
  ; @return (map)
  ([projection]
   (adapt-projection projection {}))

  ([projection {:keys [undot-keys? unkeywordize-keys?]}]
   (if (->     projection some?)
       (cond-> projection undot-keys?        (bson/undot-keys)
                          unkeywordize-keys? (json/unkeywordize-keys)))))

(defn adapt-projections
  ; @description
  ; Applies the 'adapt-projection' function on the given projections.
  ;
  ; @param (maps in vector) projections
  ; @param (map)(opt) options
  ; {:undot-keys? (boolean)(opt)
  ;  :unkeywordize-keys? (boolean)(opt)}
  ;
  ; @usage
  ; (adapt-projections [{:_id                 -1
  ;                      :my-namespace/my-key -1 ...}]
  ;                   {:unkeywordize-keys? true})
  ; =>
  ; [{"_id"                 -1
  ;   "my-namespace/my-key" -1 ...}]
  ;
  ; @return (maps in vector)
  ([projections]
   (adapt-projections projections {}))

  ([projections options]
   (letfn [(f0 [%] (adapt-projection % options))]
          (if (-> projections some?)
              (-> projections (vector/->items f0))))))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn adapt-query-id
  ; @note
  ; All changes performed recursivelly on the given query!
  ;
  ; @description
  ; Renames the given ID key to ':_id' (a MongoDB compatible identifier) within the given query (if ':id-key' is provided).
  ;
  ; @param (map) query
  ; @param (map)(opt) options
  ; {:id-key (keyword)(opt)}
  ;
  ; @usage
  ; (adapt-query-id {:my-namespace/my-id "MyObjectId" :$or [{:my-namespace/my-id "AnotherObjectId" ...}] ...}
  ;                 {:id-key :my-id})
  ; =>
  ; {:_id "MyObjectId" :$or [{:_id "AnotherObjectId" ...}] ...}
  ;
  ; @return (map)
  ([query]
   (adapt-query-id query {}))

  ([query {:keys [id-key]}]
   (letfn [(f0 [%] (adapt-document-id % {:id-key id-key}))]
          (if (-> query some?)
              (if id-key (-> query (map/->>values-by map? f0 {:on-self? true}))
                         (-> query))))))

(defn adapt-queries-id
  ; @note
  ; All changes performed recursivelly on the given queries!
  ;
  ; @description
  ; Applies the 'adapt-query-id' function on the given queries.
  ;
  ; @param (maps in vector) queries
  ; @param (map)(opt) options
  ; {:id-key (keyword)(opt)}
  ;
  ; @usage
  ; (adapt-queries-id [{:my-namespace/my-id "MyObjectId" :$or [{:my-namespace/my-id "AnotherObjectId" ...}] ...}]
  ;                   {:id-key :my-id})
  ; =>
  ; [{:_id "MyObjectId" :$or [{:_id "AnotherObjectId" ...}] ...}]
  ;
  ; @return (maps in vector)
  ([queries]
   (adapt-queries-id queries {}))

  ([queries options]
   (letfn [(f0 [%] (adapt-query-id % options))]
          (if (-> queries some?)
              (-> queries (vector/->items f0))))))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn adapt-query
  ; @note
  ; All changes performed recursivelly on the given query!
  ;
  ; @description
  ; 1. Optionally parses document ID strings into objects within the given query.
  ; 2. Optionally parses date and time strings into objects within the given query.
  ; 3. Optionally removes '.' characters from keys within the given query to prevent them misread as dot notations (BSON syntax requirement).
  ; 4. Optionally converts keyword type keys and values into strings within the given query.
  ;
  ; @param (map) query
  ; @param (map)(opt) options
  ; {:parse-id? (boolean)(opt)
  ;  :parse-timestamps? (boolean)(opt)
  ;  :undot-keys? (boolean)(opt)
  ;  :unkeywordize-keys? (boolean)(opt)
  ;  :unkeywordize-values? (boolean)(opt)}
  ;
  ; @usage
  ; (adapt-query {:_id                       "MyObjectId"
  ;               :my-namespace/my-keyword   :my-value
  ;               :my-namespace/my-string    "My value"
  ;               :my-namespace/my-timestamp "2020-04-20T16:20:00.000Z"
  ;               :$or                       [{:_id "AnotherObjectId" ...}] ...}
  ;              {:parse-id? true :parse-timestamps? true :unkeywordize-keys? true :unkeywordize-values? true})
  ; =>
  ; {"_id"                       #<ObjectId MyObjectId>
  ;  "my-namespace/my-keyword"   "*:my-value"
  ;  "my-namespace/my-string"    "My value"
  ;  "my-namespace/my-timestamp" #<DateTime 2020-04-20T16:20:00.123Z>
  ;  "$or"                       [{"_id" #<ObjectId AnotherObjectId> ...}] ...}
  ;
  ; @return (map)
  ([query]
   (adapt-query query {}))

  ([query {:keys [parse-id? parse-timestamps? undot-keys? unkeywordize-keys? unkeywordize-values?]}]
   (letfn [(f0 [%] (adapt-document % {:parse-id? parse-id?}))]
          (if (->     query some?)
              (cond-> query parse-id?            (map/->>values-by map? f0 {:on-self? true})
                            parse-timestamps?    (time/parse-timestamps)
                            undot-keys?          (bson/undot-keys)
                            unkeywordize-keys?   (json/unkeywordize-keys)
                            unkeywordize-values? (json/unkeywordize-values))))))

(defn adapt-queries
  ; @note
  ; All changes performed recursivelly on the given queries!
  ;
  ; @description
  ; Applies the 'adapt-query' function on the given queries.
  ;
  ; @param (maps in vector) queries
  ; @param (map)(opt) options
  ; {:parse-id? (boolean)(opt)
  ;  :parse-timestamps? (boolean)(opt)
  ;  :undot-keys? (boolean)(opt)
  ;  :unkeywordize-keys? (boolean)(opt)
  ;  :unkeywordize-values?? (boolean)(opt)}
  ;
  ; @usage
  ; (adapt-queries [{:_id                       "MyObjectId"
  ;                  :my-namespace/my-keyword   :my-value
  ;                  :my-namespace/my-string    "My value"
  ;                  :my-namespace/my-timestamp "2020-04-20T16:20:00.000Z"
  ;                  :$or                       [{:_id "AnotherObjectId"}] ...}]
  ;                {:parse-id? true :parse-timestamps? true :unkeywordize-keys? true :unkeywordize-values? true})
  ; =>
  ; [{"_id"                       #<ObjectId MyObjectId>
  ;   "my-namespace/my-keyword"   "*:my-value"
  ;   "my-namespace/my-string"    "My value"
  ;   "my-namespace/my-timestamp" #<DateTime 2020-04-20T16:20:00.123Z>
  ;   "$or"                       [{"_id" #<ObjectId AnotherObjectId>}] ...}]
  ;
  ; @return (maps in vector)
  ([queries]
   (adapt-queries queries {}))

  ([queries options]
   (letfn [(f0 [%] (adapt-query % options))]
          (if (-> queries some?)
              (-> queries (vector/->items f0))))))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn adapt-pipeline-stage-id
  ; @note
  ; All changes performed recursivelly on the given pipeline stage!
  ;
  ; @description
  ; Renames the given ID key to ':_id' (a MongoDB compatible identifier) within the given pipeline stage (if ':id-key' is provided).
  ;
  ; @param (map) pipeline-stage
  ; @param (map)(opt) options
  ; {:id-key (keyword)(opt)}
  ;
  ; @usage
  ; (adapt-pipeline-stage-id {:$match {:my-namespace/my-id "MyObjectId"}}
  ;                          {:id-key :my-id})
  ; =>
  ; {:$match {:_id "MyObjectId"}}
  ;
  ; @return (map)
  ([pipeline-stage]
   (adapt-pipeline-stage-id pipeline-stage {}))

  ([pipeline-stage {:keys [id-key]}]
   (letfn [(f0 [%] (adapt-document-id % {:id-key id-key}))]
          (if (-> pipeline-stage some?)
              (if id-key (-> pipeline-stage (map/->>values-by map? f0 {:on-self? true}))
                         (-> pipeline-stage))))))

(defn adapt-pipeline-stages-id
  ; @note
  ; All changes performed recursivelly on the given pipeline stages!
  ;
  ; @description
  ; Applies the 'adapt-pipeline-stage-id' function on the given pipeline stages.
  ;
  ; @param (maps in vector) pipeline-stages
  ; @param (map)(opt) options
  ; {:id-key (keyword)(opt)}
  ;
  ; @usage
  ; (adapt-pipeline-stages-id [{:$match {:my-namespace/my-id "MyObjectId"}}
  ;                            {:$sort  {:my-namespace/my-id -1}}]
  ;                          {:id-key :my-id})
  ; =>
  ; [{:$match {:my-namespace/my-id "MyObjectId"}}
  ;  {:$sort  {:my-namespace/my-id -1}}]
  ;
  ; @return (maps in vector)
  ([pipeline-stages]
   (adapt-pipeline-stages-id pipeline-stages {}))

  ([pipeline-stages options]
   (letfn [(f0 [%] (adapt-pipeline-stage-id % options))]
          (if (-> pipeline-stages some?)
              (-> pipeline-stages (vector/->items f0))))))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn adapt-pipeline-stage
  ; @note
  ; All changes performed recursivelly on the given pipeline stage!
  ;
  ; @description
  ; 1. Optionally parses document ID strings into objects within the given pipeline stage.
  ; 2. Optionally parses date and time strings into objects within the given pipeline stage.
  ; 3. Optionally removes '.' characters from keys within the given pipeline stage to prevent them misread as dot notations (BSON syntax requirement).
  ; 4. Optionally converts keyword type keys and values into strings within the given pipeline stage.
  ;
  ; @param (map) pipeline-stage
  ; @param (map)(opt) options
  ; {:parse-id? (boolean)(opt)
  ;  :parse-timestamps? (boolean)(opt)
  ;  :undot-keys? (boolean)(opt)
  ;  :unkeywordize-keys? (boolean)(opt)
  ;  :unkeywordize-values? (boolean)(opt)}
  ;
  ; @usage
  ; (adapt-pipeline-stage {:$unset [:my-namespace/my-key]}
  ;                       {:unkeywordize-keys? true}
  ; =>
  ; {"$unset" ["my-namespace/my-key"]}
  ;
  ; @return (map)
  ([pipeline-stage]
   (adapt-pipeline-stage pipeline-stage {}))

  ([pipeline-stage {:keys [parse-id? parse-timestamps? undot-keys? unkeywordize-keys? unkeywordize-values?]}]
   (letfn [(f0 [%] (println %) (adapt-document % {:parse-id? parse-id?}))]
          (if (->     pipeline-stage some?)
              (cond-> pipeline-stage parse-id?            (map/->>values-by map? f0 {:on-self? true})
                                     parse-timestamps?    (time/parse-timestamps)
                                     undot-keys?          (bson/undot-keys)
                                     unkeywordize-keys?   (map/->>values-by keyword? json/unkeywordize-key)
                                     unkeywordize-keys?   (json/unkeywordize-keys)
                                     unkeywordize-values? (json/unkeywordize-values))))))

(defn adapt-pipeline-stages
  ; @note
  ; All changes performed recursivelly on the given pipeline stages!
  ;
  ; @description
  ; Applies the 'adapt-pipeline-stage' function on the given pipeline stages.
  ;
  ; @param (maps in vector) pipeline-stages
  ; @param (map)(opt) options
  ; {:parse-id? (boolean)(opt)
  ;  :parse-timestamps? (boolean)(opt)
  ;  :undot-keys? (boolean)(opt)
  ;  :unkeywordize-keys? (boolean)(opt)
  ;  :unkeywordize-values?? (boolean)(opt)}
  ;
  ; @usage
  ; (adapt-pipeline-stages [{:$unset [:my-namespace/my-key]}
  ;                         {:$match {:_id "MyObjectId"}}]
  ;                        {:parse-id? true :unkeywordize-keys? true}
  ; =>
  ; [{"$unset" ["my-namespace/my-key"]}
  ;  {"$match" {"_id" #<ObjectId MyObjectId>}}]
  ;
  ; @return (maps in vector)
  ([pipeline-stages]
   (adapt-pipeline-stages pipeline-stages {}))

  ([pipeline-stages options]
   (letfn [(f0 [%] (adapt-pipeline-stage % options))]
          (if (-> pipeline-stages some?)
              (-> pipeline-stages (vector/->items f0))))))

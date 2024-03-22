
(ns monger.stages.normalization
    (:require [fruits.bson.api            :as bson]
              [fruits.json.api            :as json]
              [fruits.vector.api :as vector]
              [fruits.map.api :as map]
              [fruits.syntax.api :as syntax]
              [monger.tools.api        :as monger.tools]
              [time.api                   :as time]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn normalize-database-name
  ; @description
  ; Converts the given database name into a keyword.
  ;
  ; @param (string) database-name
  ;
  ; @usage
  ; (normalize-database-name "my-database")
  ; =>
  ; :my-database
  ;
  ; @return (keyword)
  [database-name]
  (if (-> database-name  some?)
      (-> database-name keyword)))

(defn normalize-databases-name
  ; @description
  ; Applies the 'normalize-database-name' function on the given database names.
  ;
  ; @param (strings in vector) database-names
  ;
  ; @usage
  ; (normalize-databases-name ["my-database" "another-database"])
  ; =>
  ; [:my-database :another-database]
  ;
  ; @return (keywords in vector)
  [database-names]
  (if (-> database-names some?)
      (-> database-names (vector/->items normalize-database-name))))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn normalize-collection-name
  ; @description
  ; Converts the given collection name into a keyword in kebab-case form.
  ;
  ; @param (string) collection-name
  ;
  ; @usage
  ; (normalize-collection-name "my_collection")
  ; =>
  ; :my-collection
  ;
  ; @return (keyword)
  [collection-name]
  (if (-> collection-name some?)
      (-> collection-name syntax/to-kebab-case keyword)))

(defn normalize-collections-name
  ; @description
  ; Applies the 'normalize-collection-name' function on the given collection names.
  ;
  ; @param (strings in vector) collection-names
  ;
  ; @usage
  ; (normalize-collections-name ["my_collection" "another_collection"])
  ; =>
  ; [:my-collection :another-collection]
  ;
  ; @return (keywords in vector)
  [collection-names]
  (if (-> collection-names some?)
      (-> collection-names (vector/->items normalize-collection-name))))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn normalize-collection-namespace
  ; @description
  ; Converts the given collection namespace into a keyword.
  ;
  ; @param (string) collection-namespace
  ;
  ; @usage
  ; (normalize-collection-namespace "my-namespace")
  ; =>
  ; :my-namespace
  ;
  ; @return (keyword)
  [collection-namespace]
  (if (-> collection-namespace some?)
      (-> collection-namespace keyword)))

(defn normalize-collections-namespace
  ; @description
  ; Applies the 'normalize-collection-namespace' function on the given collection namespaces.
  ;
  ; @param (strings in vector) collection-namespaces
  ;
  ; @usage
  ; (normalize-collections-namespaces ["my-namespace" "another-namespace"])
  ; =>
  ; [:my-namespace :another-namespace]
  ;
  ; @return (keywords in vector)
  [collection-namespaces]
  (if (-> collection-namespaces some?)
      (-> collection-namespaces (vector/->items normalize-collection-namespace))))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn normalize-locale
  ; @description
  ; Converts the given locale into a keyword.
  ;
  ; @param (string) locale
  ;
  ; @usage
  ; (normalize-locale "my-locale")
  ; =>
  ; :my-locale
  ;
  ; @return (keyword)
  [locale]
  (if (-> locale some?)
      (-> locale keyword)))

(defn normalize-locales
  ; @description
  ; Applies the 'normalize-locale' function on the given locales.
  ;
  ; @param (strings in vector) locales
  ;
  ; @usage
  ; (normalize-locales ["my-locale" "another-locale"])
  ; =>
  ; [:my-locale :another-locale]
  ;
  ; @return (keywords in vector)
  [locales]
  (if (-> locales some?)
      (-> locales (vector/->items normalize-locale))))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn unparse-id
  ; @description
  ; Unparses the given ObjectId object into a string.
  ;
  ; @param (org.bson.types.ObjectId object) id
  ;
  ; @usage
  ; (unparse-id #<ObjectId MyObjectId>)
  ; =>
  ; "MyObjectId"
  ;
  ; @return (string)
  [id]
  (if (-> id some?)
      (if (-> id monger.tools/object-id?)
          (-> id str)
          (-> id))))

(defn unparse-ids
  ; @description
  ; Applies the 'unparse-id' function on the given ObjectId objects.
  ;
  ; @param (org.bson.types.ObjectId objects in vector) ids
  ;
  ; @usage
  ; (unparse-ids [#<ObjectId MyObjectId> #<ObjectId AnotherObjectId>])
  ; =>
  ; ["MyObjectId" "AnotherObjectId"]
  ;
  ; @return (strings in vector)
  [ids]
  (if (-> ids some?)
      (-> ids (vector/->items unparse-id))))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn normalize-document-id
  ; @description
  ; Renames the ':_id' key (a MongoDB compatible identifier) to the given ID key within the given document (if ':id-key' is provided).
  ;
  ; @param (map) document
  ; @param (map)(opt) options
  ; {:id-key (keyword)(opt)}
  ;
  ; @usage
  ; (normalize-document-id {:_id "MyObjectId" ...}
  ;                        {:id-key :my-id})
  ; =>
  ; {:my-namespace/my-id "MyObjectId" ...}
  ;
  ; @return (map)
  ([document]
   (normalize-document-id document {}))

  ([document {:keys [id-key]}]
   (if (-> document some?)
       (if id-key (-> document (map/move-some :_id (map/specify-key document id-key)))
                  (-> document)))))

(defn normalize-documents-id
  ; @description
  ; Applies the 'normalize-document-id' function on the given documents.
  ;
  ; @param (maps in vector) documents
  ; @param (map)(opt) options
  ; {:id-key (keyword)(opt)}
  ;
  ; @usage
  ; (normalize-documents-id [{:_id "MyObjectId" ...}]
  ;                         {:id-key :my-id})
  ; =>
  ; [{:my-namespace/my-id "MyObjectId" ...}]
  ;
  ; @return (maps in vector)
  ([documents]
   (normalize-documents-id documents {}))

  ([documents options]
   (letfn [(f0 [%] (normalize-document-id % options))]
          (if (-> documents some?)
              (-> documents (vector/->items f0))))))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn normalize-document
  ; @description
  ; 1. Optionally converts string type keys and values into keywords within the given document (if they were converted from keywords).
  ; 2. Optionally unparses date and time objects into strings within the given document.
  ; 3. Optionally unparses the document ID object into string within the given document.
  ;
  ; @param (map) document
  ; @param (map)(opt) options
  ; {:keywordize-keys? (boolean)(opt)
  ;  :keywordize-values? (boolean)(opt)
  ;  :unparse-id? (boolean)(opt)
  ;  :unparse-timestamps? (boolean)(opt)}
  ;
  ; @example
  ; (normalize-document {"_id"                       #<ObjectId MyObjectId>
  ;                      "my-namespace/my-keyword"   "*:my-value"
  ;                      "my-namespace/my-string"    "My value"
  ;                      "my-namespace/my-timestamp" #<DateTime 2020-04-20T16:20:00.123Z> ...}
  ;                     {:keywordize-keys? true :keywordize-values? true :unparse-id? true :unparse-timestamps? true})
  ; =>
  ; {:_id                       "MyObjectId"
  ;  :my-namespace/my-keyword   :my-value
  ;  :my-namespace/my-string    "My value"
  ;  :my-namespace/my-timestamp "2020-04-20T16:20:00.000Z" ...}
  ;
  ; @return (map)
  ([document]
   (normalize-document document {}))

  ([document {:keys [keywordize-keys? keywordize-values? unparse-id? unparse-timestamps?]}]
   (if (->     document some?)
       (cond-> document keywordize-keys?    (json/keywordize-keys)
                        keywordize-values?  (json/keywordize-values)
                        unparse-id?         (map/update-some :_id unparse-id)
                        unparse-timestamps? (time/unparse-timestamps)))))

(defn normalize-documents
  ; @description
  ; Applies the 'normalize-document' function on the given documents.
  ;
  ; @param (maps in vector) documents
  ; @param (map)(opt) options
  ; {:keywordize-keys? (boolean)(opt)
  ;  :keywordize-values? (boolean)(opt)
  ;  :unparse-id? (boolean)(opt)
  ;  :unparse-timestamps? (boolean)(opt)}
  ;
  ; @usage
  ; (normalize-documents [{"_id"                       #<ObjectId MyObjectId>
  ;                        "my-namespace/my-keyword"   "*:my-value"
  ;                        "my-namespace/my-string"    "My value"
  ;                        "my-namespace/my-timestamp" #<DateTime 2020-04-20T16:20:00.123Z> ...}]
  ;                      {:keywordize-keys? true :keywordize-values? true :unparse-id? true :unparse-timestamps? true})
  ; =>
  ; [{:_id                       "MyObjectId"
  ;   :my-namespace/my-keyword   :my-value
  ;   :my-namespace/my-string    "My value"
  ;   :my-namespace/my-timestamp "2020-04-20T16:20:00.000Z" ...}]
  ;
  ; @return (maps in vector)
  ([documents]
   (normalize-documents documents {}))

  ([documents options]
   (letfn [(f0 [%] (normalize-document % options))]
          (if (-> documents some?)
              (-> documents (vector/->items f0))))))

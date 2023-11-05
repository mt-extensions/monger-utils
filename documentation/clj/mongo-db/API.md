
# mongo-db.api Clojure namespace

##### [README](../../../README.md) > [DOCUMENTATION](../../COVER.md) > mongo-db.api

### Index

- [apply-dot-notation](#apply-dot-notation)

- [apply-on-collection!](#apply-on-collection)

- [apply-on-document!](#apply-on-document)

- [build-connection!](#build-connection)

- [collection-empty?](#collection-empty)

- [connected?](#connected)

- [count-documents-by-pipeline](#count-documents-by-pipeline)

- [count-pipeline](#count-pipeline)

- [document-exists?](#document-exists)

- [duplicate-document!](#duplicate-document)

- [duplicate-documents!](#duplicate-documents)

- [generate-id](#generate-id)

- [get-all-document-count](#get-all-document-count)

- [get-collection](#get-collection)

- [get-collection-names](#get-collection-names)

- [get-collection-namespace](#get-collection-namespace)

- [get-document-by-id](#get-document-by-id)

- [get-document-by-query](#get-document-by-query)

- [get-document-count-by-query](#get-document-count-by-query)

- [get-documents-by-pipeline](#get-documents-by-pipeline)

- [get-documents-by-query](#get-documents-by-query)

- [get-first-document](#get-first-document)

- [get-last-document](#get-last-document)

- [get-pipeline](#get-pipeline)

- [get-specified-values](#get-specified-values)

- [insert-document!](#insert-document)

- [insert-documents!](#insert-documents)

- [query<-namespace](#query-namespace)

- [remove-all-documents!](#remove-all-documents)

- [remove-document!](#remove-document)

- [remove-documents!](#remove-documents)

- [reorder-documents!](#reorder-documents)

- [save-document!](#save-document)

- [save-documents!](#save-documents)

- [update-document!](#update-document)

- [update-documents!](#update-documents)

- [upsert-document!](#upsert-document)

- [upsert-documents!](#upsert-documents)

### apply-dot-notation

```
@description
Takes a nested query map as input and flattens it by collapsing nested fields
into a flat map structure that corresponds to the dot notation.
It returns a new map where nested fields are represented using dot notation.
https://www.mongodb.com/docs/manual/core/document/#dot-notation
```

```
@param (map) query
```

```
@usage
(apply-dot-notation {:user {:id "MyObjectId"}})
```

```
@example
(apply-dot-notation {:user {:id "MyObjectId"}})
=>
{:user.id "MyObjectId"}
```

```
@example
(apply-dot-notation {:user {:id "MyObjectId"}
                     :$or [{:user {:id "MyObjectId"}}]})
=>
{:user.id "MyObjectId"
 :$or [{:user.id "MyObjectId"}]}
```

```
@return (map)
```

<details>
<summary>Source code</summary>

```
(defn apply-dot-notation
  [query]
  (letfn [          (except-f [k _] (operator? k))]

         (map/collapse query {:keywordize? true :inner-except-f except-f :outer-except-f except-f :separator "."})))
```

</details>

<details>
<summary>Require</summary>

```
(ns my-namespace (:require [mongo-db.api :refer [apply-dot-notation]]))

(mongo-db.api/apply-dot-notation ...)
(apply-dot-notation              ...)
```

</details>

---

### apply-on-collection!

```
@description
Applies the given function on every document in a collection.
You can apply custom functions for preparing and postparing each document.
Returns the updated documents in a vector.
```

```
@param (string) collection-path
@param (function) f
@param (map)(opt) options
{:postpare-f (function)(opt)
  This function is applied on each input document AFTER the passed 'f'
  function is applied and before the writing.
 :prepare-f (function)(opt)
  This function is applied on each input document BEFORE the passed 'f'
  function is applied.}
```

```
@usage
(apply-on-collection! "my_collection" #(assoc % :namespace/color "Blue") {...})
```

```
@return (namespaced maps in vector)
```

<details>
<summary>Source code</summary>

```
(defn apply-on-collection!
  ([collection-path f]
   (apply-on-collection! collection-path f {}))

  ([collection-path f options]
   (if-let [collection (reader.engine/get-collection collection-path)]
           (letfn [(fi [result document]
                       (if-let [document (f document)]
                               (let [document (save-document! collection-path document options)]
                                    (conj result document))
                               (-> result)))]
                  (reduce fi [] collection)))))
```

</details>

<details>
<summary>Require</summary>

```
(ns my-namespace (:require [mongo-db.api :refer [apply-on-collection!]]))

(mongo-db.api/apply-on-collection! ...)
(apply-on-collection!              ...)
```

</details>

---

### apply-on-document!

```
@description
Applies the given function on a document.
You can apply custom functions for preparing and postparing the document.
Returns the updated document.
```

```
@param (string) collection-path
@param (string) document-id
@param (function) f
@param (map)(opt) options
{:postpare-f (function)(opt)
  This function is applied on the input document AFTER the passed 'f'
  function is applied and before the writing.
 :prepare-f (function)(opt)
  This function is applied on the input document BEFORE the passed 'f'
  function is applied.}
```

```
@usage
(apply-on-document! "my_collection" "MyObjectId" #(assoc % :namespace/color "Blue") {...})
```

```
@return (namespaced map)
```

<details>
<summary>Source code</summary>

```
(defn apply-on-document!
  ([collection-path document-id f]
   (apply-on-document! collection-path document-id f {}))

  ([collection-path document-id f options]
   (if-let [document (reader.engine/get-document-by-id collection-path document-id)]
           (if-let [document (actions.preparing/apply-input collection-path document options)]
                   (if-let [document (f document)]
                           (if-let [document (actions.postparing/apply-input collection-path document options)]
                                   (if-let [document (actions.adaptation/save-input document)]
                                           (let [result (actions.side-effects/save-and-return! collection-path document)]
                                                (actions.adaptation/save-output result)))))))))
```

</details>

<details>
<summary>Require</summary>

```
(ns my-namespace (:require [mongo-db.api :refer [apply-on-document!]]))

(mongo-db.api/apply-on-document! ...)
(apply-on-document!              ...)
```

</details>

---

### build-connection!

```
@param (string) database-name
@param (string) database-host
@param (integer) database-port
```

```
@usage
(build-connection! "my-database" "0.0.0.1" 4200)
```

<details>
<summary>Source code</summary>

```
(defn build-connection!
  [database-name database-host database-port]
  (let [^MongoOptions  mongo-options  (mcr/mongo-options {:threads-allowed-to-block-for-connection-multiplier 300})
        ^ServerAddress server-address (mcr/server-address database-host  database-port)
                       connection     (mcr/connect        server-address mongo-options)
                       reference      (mcr/get-db         connection     database-name)]
       (swap! connection.state/REFERENCES assoc database-name reference)))
```

</details>

<details>
<summary>Require</summary>

```
(ns my-namespace (:require [mongo-db.api :refer [build-connection!]]))

(mongo-db.api/build-connection! ...)
(build-connection!              ...)
```

</details>

---

### collection-empty?

```
@param (string) collection-path
```

```
@usage
(collection-empty? "my_collection")
```

```
@return (boolean)
```

<details>
<summary>Source code</summary>

```
(defn collection-empty?
  [collection-path]
  (-> collection-path reader.env/count-documents zero?))
```

</details>

<details>
<summary>Require</summary>

```
(ns my-namespace (:require [mongo-db.api :refer [collection-empty?]]))

(mongo-db.api/collection-empty? ...)
(collection-empty?              ...)
```

</details>

---

### connected?

```
@description
Checks whether a specific database has active connection.
If no database name passed it checks the only stored database reference.
```

```
@param (string)(opt) database-name
```

```
@usage
(connected?)
```

```
@usage
(connected? "my-database")
```

```
@return (boolean)
```

<details>
<summary>Source code</summary>

```
(defn connected?
  ([]
   (let [database-name (connection.utils/default-database-name)]
        (connected? database-name)))

  ([database-name]
   (core.env/command database-name {:ping 1 :warn? false})))
```

</details>

<details>
<summary>Require</summary>

```
(ns my-namespace (:require [mongo-db.api :refer [connected?]]))

(mongo-db.api/connected? ...)
(connected?              ...)
```

</details>

---

### count-documents-by-pipeline

```
@param (string) collection-path
@param (maps in vector) pipeline
@param (map)(opt) options
{:locale (string)(opt)
  Default: "en"
  https://www.mongodb.com/docs/manual/reference/collation-locales-defaults}
```

```
@usage
(count-documents-by-pipeline "my_collection" [...])
```

```
@usage
(count-documents-by-pipeline "my_collection" (count-pipeline {...}))
```

```
@return (integer)
```

<details>
<summary>Source code</summary>

```
(defn count-documents-by-pipeline
  ([collection-path pipeline]
   (count-documents-by-pipeline collection-path pipeline {}))

  ([collection-path pipeline options]
   (if-let [documents (aggregation.engine/process collection-path pipeline options)]
           (-> documents count)
           (-> 0))))
```

</details>

<details>
<summary>Require</summary>

```
(ns my-namespace (:require [mongo-db.api :refer [count-documents-by-pipeline]]))

(mongo-db.api/count-documents-by-pipeline ...)
(count-documents-by-pipeline              ...)
```

</details>

---

### count-pipeline

```
@param (map) pipeline-props
{:field-pattern (map)(opt)
 :filter-pattern (map)(opt)
 :search-pattern (map)(opt)}
```

```
@usage
(count-pipeline {:field-pattern  {:namespace/name {:$concat [:$namespace/first-name " " :$namespace/last-name]}
                 :filter-pattern {:namespace/my-keyword :my-value
                                  :$or [{:namespace/my-boolean   false}
                                        {:namespace/my-boolean   nil}]}
                 :search-pattern {:$or [{:namespace/my-string   "My value"}]
                                        {:namespace/your-string "Your value"}]}})
```

```
@return (maps in vector)
```

<details>
<summary>Source code</summary>

```
(defn count-pipeline
  [{:keys [field-pattern filter-pattern search-pattern]}]
  (cond-> [] field-pattern (conj {"$addFields"     (add-fields-query field-pattern)})
             :match        (conj {"$match" {"$and" [(filter-query filter-pattern)
                                                    (search-query search-pattern)]}})))
```

</details>

<details>
<summary>Require</summary>

```
(ns my-namespace (:require [mongo-db.api :refer [count-pipeline]]))

(mongo-db.api/count-pipeline ...)
(count-pipeline              ...)
```

</details>

---

### document-exists?

```
@param (string) collection-path
@param (string) document-id
```

```
@usage
(document-exists? "my_collection" "MyObjectId")
```

```
@return (boolean)
```

<details>
<summary>Source code</summary>

```
(defn document-exists?
  [collection-path document-id]
  (boolean (if-let [document-id (reader.adaptation/document-id-input document-id)]
                   (reader.env/find-map-by-id collection-path document-id))))
```

</details>

<details>
<summary>Require</summary>

```
(ns my-namespace (:require [mongo-db.api :refer [document-exists?]]))

(mongo-db.api/document-exists? ...)
(document-exists?              ...)
```

</details>

---

### duplicate-document!

```
@description
Duplicates a document in a collection.
Returns the copy document if the duplicating was successful.
```

```
@param (string) collection-path
@param (string) document-id
@param (map)(opt) options
{:changes (namespaced map)(opt)
  The copy document will include the provided changes.
 :label-key (namespaced keyword)(opt)
  Which key of the copy document gets the copy marker ("#2", "#3", etc.)
  appended to its value.
 :ordered? (boolean)(opt)
  Set to TRUE when duplicating a document in an ordered collection!
  Default: false
 :postpare-f (function)(opt)
  This function is applied on the copy document before the writing.
 :prepare-f (function)(opt)
  This function is applied on the copy document after it is derived from the original document.}
```

```
@usage
(duplicate-document! "my_collection" "MyObjectId" {...})
```

```
@example
(duplicate-document! "my_collection" "MyObjectId" {...})
=>
{:namespace/id "MyObjectId" :namespace/label "My document"}
```

```
@example
(duplicate-document! "my_collection" "MyObjectId" {:label-key :namespace/label})
=>
{:namespace/id "MyObjectId" :namespace/label "My document #2"}
```

```
@return (namespaced map)
{:namespace/id (string)}
```

<details>
<summary>Source code</summary>

```
(defn duplicate-document!
  ([collection-path document-id]
   (duplicate-document! collection-path document-id {}))

  ([collection-path document-id {:keys [ordered?] :as options}]
   (if ordered? (duplicate-ordered-document!   collection-path document-id options)
                (duplicate-unordered-document! collection-path document-id options))))
```

</details>

<details>
<summary>Require</summary>

```
(ns my-namespace (:require [mongo-db.api :refer [duplicate-document!]]))

(mongo-db.api/duplicate-document! ...)
(duplicate-document!              ...)
```

</details>

---

### duplicate-documents!

```
@description
Duplicates multiple documents in a collection.
Returns the copy documents in a vector if the duplicating was successful.
```

```
@param (string) collection-path
@param (strings in vector) document-ids
@param (map)(opt) options
{:label-key (namespaced keyword)(opt)
  Which key of the copy documents gets the copy marker ("#2", "#3", etc.)
  appended to its value.
 :ordered? (boolean)(opt)
  Set to TRUE when duplicating documents in an ordered collection!
  Default: false
 :postpare-f (function)(opt)
  This function is applied on each copy document before the writing.
 :prepare-f (function)(opt)
  This function is applied on each copy document after they are derived from
  the original documents.
 :prototype-f (function)(opt)
  This function is applied on each input document before checking or preparing.
  Must return a namespaced map!}
```

```
@usage
(duplicate-documents! "my_collection" ["MyObjectId" "YourObjectId"] {...})
```

```
@example
(duplicate-documents! "my_collection" ["MyObjectId" "YourObjectId"] {...})
=>
[{...} {...}]
```

```
@return (namespaced maps in vector)
```

<details>
<summary>Source code</summary>

```
(defn duplicate-documents!
  ([collection-path document-ids]
   (duplicate-documents! collection-path document-ids {}))

  ([collection-path document-ids options]
   (vector/->items document-ids #(duplicate-document! collection-path % options))))
```

</details>

<details>
<summary>Require</summary>

```
(ns my-namespace (:require [mongo-db.api :refer [duplicate-documents!]]))

(mongo-db.api/duplicate-documents! ...)
(duplicate-documents!              ...)
```

</details>

---

### generate-id

```
@description
Returns a randomly generated ObjectId for documents.
```

```
@usage
(generate-id)
```

```
@return (string)
```

<details>
<summary>Source code</summary>

```
(defn generate-id
  []
  (str (ObjectId.)))
```

</details>

<details>
<summary>Require</summary>

```
(ns my-namespace (:require [mongo-db.api :refer [generate-id]]))

(mongo-db.api/generate-id)
(generate-id)
```

</details>

---

### get-all-document-count

```
@param (string) collection-path
```

```
@usage
(get-all-document-count "my_collection")
```

```
@return (integer)
```

<details>
<summary>Source code</summary>

```
(defn get-all-document-count
  [collection-path]
  (reader.env/count-documents collection-path))
```

</details>

<details>
<summary>Require</summary>

```
(ns my-namespace (:require [mongo-db.api :refer [get-all-document-count]]))

(mongo-db.api/get-all-document-count ...)
(get-all-document-count              ...)
```

</details>

---

### get-collection

```
@param (string) collection-path
@param (map)(opt) options
{:projection (namespaced map)(opt)
 :prototype-f (function)(opt)
  This function is applied on each output document.}
```

```
@usage
(get-collection "my_collection")
```

```
@usage
(get-collection "my_collection" {...})
```

```
@example
(get-collection "my_collection" {:projection {:namespace/id          1
                                              :namespace/your-string 1}})
=>
[{:namespace/id          "MyObjectId"
  :namespace/your-string "Your value"}]
```

```
@example
(get-collection "my_collection" {:prototype-f :namespace/my-string}})
=>
["MY value" "Your value"]
```

```
@return (namespaced maps or * in vector)
[{:namespace/id (string)}]
```

<details>
<summary>Source code</summary>

```
(defn get-collection
  ([collection-path]
   (if-let [collection (reader.env/find-maps collection-path {})]
           (vector/->items collection #(reader.adaptation/find-output %))))

  ([collection-path {:keys [projection] :as options}]
   (if-let [projection (reader.adaptation/find-projection projection)]
           (if-let [collection (reader.env/find-maps collection-path {} projection)]
                   (letfn [(f [document] (as-> document % (reader.adaptation/find-output %)
                                                          (reader.prototyping/find-output collection-path % options)))]
                          (vector/->items collection f))))))
```

</details>

<details>
<summary>Require</summary>

```
(ns my-namespace (:require [mongo-db.api :refer [get-collection]]))

(mongo-db.api/get-collection ...)
(get-collection              ...)
```

</details>

---

### get-collection-names

```
@param (string)(opt) database-name
```

```
@usage
(get-collection-names)
```

```
@usage
(get-collection-names "my-database")
```

```
@return (strings in vector)
```

<details>
<summary>Source code</summary>

```
(defn get-collection-names
  ([]
   (let [database-name (connection.utils/default-database-name)]
        (get-collection-names database-name)))

  ([database-name]
   (if-let [database-reference (get @connection.state/REFERENCES database-name)]
           (-> database-reference mdb/get-collection-names vec)
           (try (throw (Exception. core.errors/NO-DATABASE-REFERENCE-FOUND-ERROR))
                (catch Exception e (println (str e "\n" {:database-name database-name})))))))
```

</details>

<details>
<summary>Require</summary>

```
(ns my-namespace (:require [mongo-db.api :refer [get-collection-names]]))

(mongo-db.api/get-collection-names ...)
(get-collection-names              ...)
```

</details>

---

### get-collection-namespace

```
@param (string) collection-path
```

```
@usage
(get-collection-namespace "my_collection")
```

```
@return (keyword)
```

<details>
<summary>Source code</summary>

```
(defn get-collection-namespace
  [collection-path]
  (let [collection (reader.env/find-maps collection-path {})]
       (-> collection first map/get-namespace)))
```

</details>

<details>
<summary>Require</summary>

```
(ns my-namespace (:require [mongo-db.api :refer [get-collection-namespace]]))

(mongo-db.api/get-collection-namespace ...)
(get-collection-namespace              ...)
```

</details>

---

### get-document-by-id

```
@param (string) collection-path
@param (string) document-id
@param (map)(opt) options
{:projection (namespaced map)(opt)
 :prototype-f (function)(opt)
  This function is applied on the output document.}
```

```
@usage
(get-document-by-id "my_collection" "MyObjectId")
```

```
@usage
(get-document-by-id "my_collection" "MyObjectId" {:prototype-f :namespace/my-keyword})
```

```
@example
(get-document-by-id "my_collection" "MyObjectId"
                    {:projection {:namespace/id          1
                                  :namespace/your-string 1}})
=>
{:namespace/id          "MyObjectId"
 :namespace/your-string "Your value"}
```

```
@example
(get-document-by-id "my_collection" "MyObjectId" {:prototype-f :namespace/my-string})
=>
"My value"
```

```
@return (namespaced map or *)
{:namespace/id (string)}
```

<details>
<summary>Source code</summary>

```
(defn get-document-by-id
  ([collection-path document-id]
   (if-let [document-id (reader.adaptation/document-id-input document-id)]
           (if-let [document (reader.env/find-map-by-id collection-path document-id)]
                   (reader.adaptation/find-output document))))

  ([collection-path document-id {:keys [projection] :as options}]
   (if-let [document-id (reader.adaptation/document-id-input document-id)]
           (if-let [projection (reader.adaptation/find-projection projection)]
                   (if-let [document (reader.env/find-map-by-id collection-path document-id projection)]
                           (as-> document % (reader.adaptation/find-output %)
                                            (reader.prototyping/find-output collection-path % options)))))))
```

</details>

<details>
<summary>Require</summary>

```
(ns my-namespace (:require [mongo-db.api :refer [get-document-by-id]]))

(mongo-db.api/get-document-by-id ...)
(get-document-by-id              ...)
```

</details>

---

### get-document-by-query

```
@param (string) collection-path
@param (map) query
@param (map)(opt) options
{:projection (namespaced map)(opt)
 :prototype-f (function)(opt)
  This function is applied on the output document.}
```

```
@usage
(get-document-by-query "my_collection" {:namespace/my-keyword :my-value})
```

```
@usage
(get-document-by-query "my_collection" {:namespace/my-keyword :my-value} {:prototype-f :namespace/my-keyword})
```

```
@usage
(get-document-by-query "my_collection" {:$or [{...} {...}]})
```

```
@example
(get-document-by-query "my_collection" {:namespace/my-keyword :my-value}
                                       {:projection {:namespace/id          1
                                                     :namespace/your-string 1}})
=>
{:namespace/id          "MyObjectId"
 :namespace/your-string "Your value"}
```

```
@example
(get-document-by-query "my_collection" {:namespace/my-keyword :my-value}
                                       {:prototype-f :namespace/my-string})
=>
"My value"
```

```
@return (namespaced map or *)
{:namespace/id (string)}
```

<details>
<summary>Source code</summary>

```
(defn get-document-by-query
  ([collection-path query]
   (if-let [query (-> query reader.checking/find-query reader.adaptation/find-query)]
           (if-let [document (reader.env/find-one-as-map collection-path query)]
                   (reader.adaptation/find-output document))))

  ([collection-path query {:keys [projection] :as options}]
   (if-let [query (-> query reader.checking/find-query reader.adaptation/find-query)]
           (if-let [projection (reader.adaptation/find-projection projection)]
                   (if-let [document (reader.env/find-one-as-map collection-path query projection)]
                           (as-> document % (reader.adaptation/find-output %)
                                            (reader.prototyping/find-output collection-path % options)))))))
```

</details>

<details>
<summary>Require</summary>

```
(ns my-namespace (:require [mongo-db.api :refer [get-document-by-query]]))

(mongo-db.api/get-document-by-query ...)
(get-document-by-query              ...)
```

</details>

---

### get-document-count-by-query

```
@param (string) collection-path
@param (map) query
```

```
@usage
(get-document-count-by-query "my_collection" {:namespace/my-keyword :my-value})
```

```
@usage
(get-document-count-by-query "my_collection" {:$or [{...} {...}]})
```

```
@usage
(get-document-count-by-query "my_collection" {:namespace/my-keyword  :my-value}
                                              :namespace/your-string "Your value"})
```

```
@return (integer)
```

<details>
<summary>Source code</summary>

```
(defn get-document-count-by-query
  [collection-path query]
  (if-let [query (-> query reader.checking/find-query reader.adaptation/find-query)]
          (reader.env/count-documents-by-query collection-path query)))
```

</details>

<details>
<summary>Require</summary>

```
(ns my-namespace (:require [mongo-db.api :refer [get-document-count-by-query]]))

(mongo-db.api/get-document-count-by-query ...)
(get-document-count-by-query              ...)
```

</details>

---

### get-documents-by-pipeline

```
@param (string) collection-path
@param (maps in vector) pipeline
@param (map)(opt) options
{:locale (string)(opt)
  Default: "en"
  https://www.mongodb.com/docs/manual/reference/collation-locales-defaults
 :prototype-f (function)(opt)
  This function is applied on each output document.}
```

```
@usage
(get-documents-by-pipeline "my_collection" [...])
```

```
@usage
(get-documents-by-pipeline "my_collection" (get-pipeline {...}))
```

```
@usage
(get-documents-by-pipeline "my_collection" [...] {:locale "en"})
```

```
@usage
(get-documents-by-pipeline "my_collection" [...] {:prototype-f :namespace/my-string})
```

```
@return (namespaced maps or * in vector)
```

<details>
<summary>Source code</summary>

```
(defn get-documents-by-pipeline
  ([collection-path pipeline]
   (get-documents-by-pipeline collection-path pipeline {}))

  ([collection-path pipeline options]
   (if-let [documents (aggregation.engine/process collection-path pipeline options)]
           (letfn [(f [document] (as-> document % (reader.adaptation/find-output %)
                                                  (reader.prototyping/find-output collection-path % options)))]
                  (vector/->items documents f))
           (-> []))))
```

</details>

<details>
<summary>Require</summary>

```
(ns my-namespace (:require [mongo-db.api :refer [get-documents-by-pipeline]]))

(mongo-db.api/get-documents-by-pipeline ...)
(get-documents-by-pipeline              ...)
```

</details>

---

### get-documents-by-query

```
@param (string) collection-path
@param (map) query
@param (map)(opt) options
{:projection (namespaced map)(opt)
 :prototype-f (function)(opt)
  This function is applied on each output document.}
```

```
@usage
(get-documents-by-query "my_collection" {:namespace/my-keyword :my-value})
```

```
@usage
(get-documents-by-query "my_collection" {:$or [{...} {...}]})
```

```
@example
(get-documents-by-query "my_collection" {:namespace/my-keyword :my-value}
                                        {:projection {:namespace/id          1
                                                      :namespace/your-string 1}})
=>
[{:namespace/id          "MyObjectId"
  :namespace/my-keyword  :my-value
  :namespace/your-string "Your value"}]
```

```
@example
(get-documents-by-query "my_collection" {:namespace/my-keyword :my-value}
                                        {:prototype-f :namespace/my-string})
=>
["My value" "Your value"]
```

```
@return (namespaced maps or * in vector)
[{:namespace/id (string)}]
```

<details>
<summary>Source code</summary>

```
(defn get-documents-by-query
  ([collection-path query]
   (if-let [query (-> query reader.checking/find-query reader.adaptation/find-query)]
           (if-let [documents (reader.env/find-maps collection-path query)]
                   (vector/->items documents #(reader.adaptation/find-output %)))))

  ([collection-path query {:keys [projection] :as options}]
   (if-let [query (-> query reader.checking/find-query reader.adaptation/find-query)]
           (if-let [projection (reader.adaptation/find-projection projection)]
                   (if-let [documents (reader.env/find-maps collection-path query projection)]
                           (letfn [(f [document] (as-> document % (reader.adaptation/find-output %)
                                                                  (reader.prototyping/find-output collection-path % options)))]
                                  (vector/->items documents f)))))))
```

</details>

<details>
<summary>Require</summary>

```
(ns my-namespace (:require [mongo-db.api :refer [get-documents-by-query]]))

(mongo-db.api/get-documents-by-query ...)
(get-documents-by-query              ...)
```

</details>

---

### get-first-document

```
@param (string) collection-path
@param (map)(opt) options
{:prototype-f (function)(opt)
  This function is applied on the output document.}
```

```
@usage
(get-first-document "my_collection")
```

```
@usage
(get-first-document "my_collection" {:prototype-f :namespace/my-string})
```

```
@return (namespaced map or *)
```

<details>
<summary>Source code</summary>

```
(defn get-first-document
  [collection-path]
  (let [collection (get-collection collection-path)]
       (first collection)))
```

</details>

<details>
<summary>Require</summary>

```
(ns my-namespace (:require [mongo-db.api :refer [get-first-document]]))

(mongo-db.api/get-first-document ...)
(get-first-document              ...)
```

</details>

---

### get-last-document

```
@param (string) collection-path
```

```
@usage
(get-last-document "my_collection")
```

```
@usage
(get-last-document "my_collection" {:prototype-f :namespace/my-string})
```

```
@return (namespaced map or *)
```

<details>
<summary>Source code</summary>

```
(defn get-last-document
  [collection-path]
  (let [collection (get-collection collection-path)]
       (last collection)))
```

</details>

<details>
<summary>Require</summary>

```
(ns my-namespace (:require [mongo-db.api :refer [get-last-document]]))

(mongo-db.api/get-last-document ...)
(get-last-document              ...)
```

</details>

---

### get-pipeline

```
@param (map) pipeline-props
{:field-pattern (map)(opt)
 :filter-pattern (map)(opt)
 :max-count (integer)(opt)
 :search-pattern (map)(opt)
 :skip (integer)(opt)
 :sort-pattern (map)(opt)
 :unset-pattern (namespaced keywords in vector)(opt)}
```

```
@usage
(get-pipeline {:field-pattern  {:namespace/name {:$concat [:$namespace/first-name " " :$namespace/last-name]}
               :filter-pattern {:namespace/my-keyword :my-value
                                :$or [{:namespace/my-boolean  false}
                                      {:namespace/my-boolean  nil}]}
               :search-pattern {:$or [{:namespace/my-string   "My value"}
                                      {:namespace/your-string "Your value"}]}
               :sort-pattern   {:namespace/my-string -1}
               :unset-pattern  [:namespace/my-string :namespace/your-string]
               :max-count 20
               :skip      40})
```

```
@return (maps in vector)
```

<details>
<summary>Source code</summary>

```
(defn get-pipeline
  [{:keys [field-pattern filter-pattern max-count search-pattern skip sort-pattern unset-pattern]}]
  (cond-> [] field-pattern (conj {"$addFields"     (add-fields-query field-pattern)})
             :match        (conj {"$match" {"$and" [(filter-query     filter-pattern)
                                                    (search-query     search-pattern)]}})
             sort-pattern  (conj {"$sort"           (sort-query       sort-pattern)})
             unset-pattern (conj {"$unset"          (unset-query      unset-pattern)})
             skip          (conj {"$skip"           (->               skip)})
             max-count     (conj {"$limit"          (->               max-count)})))
```

</details>

<details>
<summary>Require</summary>

```
(ns my-namespace (:require [mongo-db.api :refer [get-pipeline]]))

(mongo-db.api/get-pipeline ...)
(get-pipeline              ...)
```

</details>

---

### get-specified-values

```
@param (string) collection-path
@param (keywords in vector) specified-keys
@param (function)(opt) test-f
Default: some?
```

```
@example
(get-specified-values "my_collection" [:my-key :your-key] string?)
=>
{:my-key   ["..." "..."]
 :your-key ["..." "..."]}
```

```
@return (map)
```

<details>
<summary>Source code</summary>

```
(defn get-specified-values
  ([collection-path specified-keys]
   (get-specified-values collection-path specified-keys some?))

  ([collection-path specified-keys test-f]
   (letfn [(f [result document]
              (letfn [(f [result k]
                         (let [v (get document k)]
                              (if (test-f v)
                                  (update result k vector/conj-item-once v)
                                  (->     result))))]
                     (reduce f result specified-keys)))]
          (let [collection (get-collection collection-path)]
               (reduce f {} collection)))))
```

</details>

<details>
<summary>Require</summary>

```
(ns my-namespace (:require [mongo-db.api :refer [get-specified-values]]))

(mongo-db.api/get-specified-values ...)
(get-specified-values              ...)
```

</details>

---

### insert-document!

```
@description
Inserts a document into a collection.
You can apply custom functions for preparing and prototyping the document.
Returns the inserted document.
```

```
@param (string) collection-path
@param (namespaced map) document
No need to be a namespaced map if using a prototype function that converts it
into a namespaced form!
{:namespace/id (string)(opt)}
@param (map)(opt) options
{:ordered? (boolean)(opt)
  Set to TRUE when inserting a document into an ordered collection!
  Default: false
 :prepare-f (function)(opt)
  This function is applied on the input document right before writing.
 :prototype-f (function)(opt)
  This function is applied on the input document first before any checking
  or preparing. Must return a namespaced map!}
```

```
@usage
(insert-document! "my_collection" {:namespace/id "MyObjectId" ...} {...})
```

```
@example
(insert-document! "my_collection" {:namespace/id "MyObjectId" ...} {...})
=>
{:namespace/id "MyObjectId" ...}
```

```
@return (namespaced map)
{:namespace/id (string)}
```

<details>
<summary>Source code</summary>

```
(defn insert-document!
  ([collection-path document]
   (insert-document! collection-path document {}))

  ([collection-path document options]
   (if-let [document (as-> document % (actions.prototyping/insert-input collection-path % options)
                                      (actions.checking/insert-input %)
                                      (actions.preparing/insert-input collection-path % options)
                                      (actions.adaptation/insert-input %))]
           (if-let [result (actions.side-effects/insert-and-return! collection-path document)]
                   (actions.adaptation/insert-output result)))))
```

</details>

<details>
<summary>Require</summary>

```
(ns my-namespace (:require [mongo-db.api :refer [insert-document!]]))

(mongo-db.api/insert-document! ...)
(insert-document!              ...)
```

</details>

---

### insert-documents!

```
@description
Inserts multiple documents into a collection.
You can apply custom functions for preparing and prototyping each document.
Returns the inserted documents in a vector.
```

```
@param (string) collection-path
@param (namespaced maps in vector) documents
No need to be a namespaced map if using a prototype function that converts it
into a namespaced form!
[{:namespace/id (string)(opt)}]
@param (map)(opt) options
{:ordered? (boolean)(opt)
  Set to TRUE when inserting documents into an ordered collection!
  Default: false
 :prepare-f (function)(opt)
  This function is applied on each input document right before writing.
 :prototype-f (function)(opt)
  This function is applied on each input document first before any checking
  or preparing. Must return a namespaced map!}
```

```
@usage
(insert-documents! "my_collection" [{:namespace/id "12ab3cd4efg5h6789ijk0420" ...}] {...})
```

```
@example
(insert-documents! "my_collection" [{:namespace/id "12ab3cd4efg5h6789ijk0420" ...}] {...})
=>
[{:namespace/id "12ab3cd4efg5h6789ijk0420" ...}]
```

```
@return (namespaced maps in vector)
[{:namespace/id (string)}]
```

<details>
<summary>Source code</summary>

```
(defn insert-documents!
  ([collection-path documents]
   (insert-documents! collection-path documents {}))

  ([collection-path documents options]
   (vector/->items documents #(insert-document! collection-path % options))))
```

</details>

<details>
<summary>Require</summary>

```
(ns my-namespace (:require [mongo-db.api :refer [insert-documents!]]))

(mongo-db.api/insert-documents! ...)
(insert-documents!              ...)
```

</details>

---

### query<-namespace

```
@description
Applies the given namespace to every key in the given query excluding keys
that are operators.
It supports optional recursive application of the namespace to nested maps
when the 'recur?' option is set to TRUE.
Using the dot notation could lead to multi-namespaced keywords therefore
this function applies the given namespace by simply prepending it to keys
without changing the key's structure:
(query<-namespace {:a/b.c/d.e/f "My string"} :my-namespace)
=>
{:my-namespace/a/b.c/d.e/f "My string"}
Using multi-namespaced keywords could be a problem with future versions of Clojure!
https://clojuredocs.org/clojure.core/keyword
```

```
@param (map) query
@param (keyword) namespace
@param (map)(opt) options
{:recur? (boolean)(opt)
  Default: false}
```

```
@usage
(query<-namespace {:id         "MyObjectId"
                   :my-keyword :my-value
                   :$or        [{:id "YourObjectId"}]}
                  :my-namespace)
```

```
@example
(query<-namespace {:id         "MyObjectId"
                   :my-keyword :my-value
                   :$or        [{:id "YourObjectId"}]}
                  :my-namespace)
=>
{:my-namespace/id         "MyObjectId"
 :my-namespace/my-keyword :my-value
 :$or                     [{:id "YourObjectId"}]}
```

```
@example
(query<-namespace {:id         "MyObjectId"
                   :my-keyword :my-value
                   :my-map     {:id "YourObjectId"}}
                  :my-namespace
                  {:recur? true})
=>
{:my-namespace/id         "MyObjectId"
 :my-namespace/my-keyword :my-value
 :my-namespace/my-map     {:my-namespace/id "YourObjectId"}}
```

```
@return (namespaced map)
```

<details>
<summary>Source code</summary>

```
(defn query<-namespace
  ([query namespace]
   (query<-namespace query namespace {}))

  ([query namespace {:keys [recur?]}]
   (letfn [(f [k] (if (-> k operator?)
                      (-> k)

                      (as-> k % (str  %)
                                (subs % 1)
                                (str (name namespace) "/" %))))]

          (if recur? (map/->>keys query f)
                     (map/->keys  query f)))))
```

</details>

<details>
<summary>Require</summary>

```
(ns my-namespace (:require [mongo-db.api :refer [query<-namespace]]))

(mongo-db.api/query<-namespace ...)
(query<-namespace              ...)
```

</details>

---

### remove-all-documents!

```
@description
Removes all documents from a collection.
Returns the document IDs (?) in a vector if the removal was successful.
```

```
@param (string) collection-path
```

```
@usage
(remove-all-documents! "my_collection")
```

```
@return (?)
```

<details>
<summary>Source code</summary>

```
(defn remove-all-documents!
  [collection-path]
  (actions.side-effects/drop! collection-path))
```

</details>

<details>
<summary>Require</summary>

```
(ns my-namespace (:require [mongo-db.api :refer [remove-all-documents!]]))

(mongo-db.api/remove-all-documents! ...)
(remove-all-documents!              ...)
```

</details>

---

### remove-document!

```
@description
Removes a document from a collection.
Returns the document ID if the removal was successful.
```

```
@param (string) collection-path
@param (string) document-id
@param (map)(opt) options
{:ordered? (boolean)
  Set to TRUE when removing a document from an ordered collection!
  Default: false}
```

```
@usage
(remove-document "my_collection" "MyObjectId" {...})
```

```
@example
(remove-document "my_collection" "MyObjectId" {...})
=>
"MyObjectId"
```

```
@return (string)
```

<details>
<summary>Source code</summary>

```
(defn remove-document!
  ([collection-path document-id]
   (remove-document! collection-path document-id {}))

  ([collection-path document-id {:keys [ordered?] :as options}]
   (if ordered? (remove-ordered-document!   collection-path document-id options)
                (remove-unordered-document! collection-path document-id options))))
```

</details>

<details>
<summary>Require</summary>

```
(ns my-namespace (:require [mongo-db.api :refer [remove-document!]]))

(mongo-db.api/remove-document! ...)
(remove-document!              ...)
```

</details>

---

### remove-documents!

```
@description
Removes multiple documents from a collection.
Returns the document IDs in a vector if the removal was successful.
```

```
@param (string) collection-path
@param (strings in vector) document-ids
@param (map)(opt) options
{:ordered? (boolean)
  Set to TRUE when removing documents from an ordered collection!
  Default: false}
```

```
@usage
(remove-documents! "my_collection" ["MyObjectId" "YourObjectId"] {...})
```

```
@example
(remove-documents! "my_collection" ["MyObjectId" "YourObjectId"] {...})
=>
["MyObjectId" "YourObjectId"]
```

```
@return (strings in vector)
```

<details>
<summary>Source code</summary>

```
(defn remove-documents!
  ([collection-path document-ids]
   (remove-documents! collection-path document-ids {}))

  ([collection-path document-ids options]
   (vector/->items document-ids #(remove-document! collection-path % options))))
```

</details>

<details>
<summary>Require</summary>

```
(ns my-namespace (:require [mongo-db.api :refer [remove-documents!]]))

(mongo-db.api/remove-documents! ...)
(remove-documents!              ...)
```

</details>

---

### reorder-documents!

```
@description
Reorders documents in a collection.
Returns the (?).
```

```
@param (string) collection-path
@param (vectors in vector) document-order
[[(string) document-id
  (integer) document-dex]]
```

```
@usage
(reorder-documents "my_collection" [["MyObjectId" 1] ["YourObjectId" 2]])
```

```
@return (vectors in vector)
```

<details>
<summary>Source code</summary>

```
(defn reorder-documents!
  [collection-path document-order]
  (let [namespace (reader.engine/get-collection-namespace collection-path)
        order-key (keyword/add-namespace :order namespace)]
       (letfn [(f [[document-id document-dex]]
                  (if-let [document-id (actions.adaptation/document-id-input document-id)]
                          (let [result (actions.side-effects/update! collection-path {:_id document-id}
                                                                     {"$set" {order-key document-dex}})]
                               (if (mrt/acknowledged? result)
                                   (-> [document-id document-dex])))))]
              (vector/->items document-order f))))
```

</details>

<details>
<summary>Require</summary>

```
(ns my-namespace (:require [mongo-db.api :refer [reorder-documents!]]))

(mongo-db.api/reorder-documents! ...)
(reorder-documents!              ...)
```

</details>

---

### save-document!

```
@description
Saves a document to a collection.
You can apply custom functions for preparing and prototyping the document.
Returns the saved document.
```

```
@param (string) collection-path
@param (namespaced map) document
No need to be a namespaced map if using a prototype function that converts it
into a namespaced form!
{:namespace/id (string)(opt)}
@param (map)(opt) options
{:ordered? (boolean)(opt)
  Set to TRUE when saving a document to an ordered collection!
  Default: false
 :prepare-f (function)(opt)
  This function is applied on the input document right before writing.
 :prototype-f (function)(opt)
  This function is applied on the input document first before any checking
  or preparing. Must return a namespaced map!}
```

```
@usage
(save-document! "my_collection" {:namespace/id "MyObjectId" ...} {...})
```

```
@example
(save-document! "my_collection" {:namespace/id "MyObjectId" ...} {...})
=>
{:namespace/id "MyObjectId" ...}
```

```
@return (namespaced map)
{:namespace/id (string)}
```

<details>
<summary>Source code</summary>

```
(defn save-document!
  ([collection-path document]
   (save-document! collection-path document {}))

  ([collection-path document options]
   (if-let [document (as-> document % (actions.prototyping/save-input collection-path % options)
                                      (actions.checking/save-input %)
                                      (actions.preparing/save-input collection-path % options)
                                      (actions.adaptation/save-input %))]
           (if-let [result (actions.side-effects/save-and-return! collection-path document)]
                   (actions.adaptation/save-output result)))))
```

</details>

<details>
<summary>Require</summary>

```
(ns my-namespace (:require [mongo-db.api :refer [save-document!]]))

(mongo-db.api/save-document! ...)
(save-document!              ...)
```

</details>

---

### save-documents!

```
@description
Saves multiple documents to a collection.
You can apply custom functions for preparing and prototyping each document.
Returns the saved documents in a vector.
```

```
@param (string) collection-path
@param (namespaced maps in vector) documents
No need to be a namespaced map if using a prototype function that converts it
into a namespaced form!
[{:namespace/id (string)(opt)}]
@param (map)(opt) options
{:ordered? (boolean)(opt)
  Set to TRUE when saving documents to an ordered collection!
  Default: false
 :prepare-f (function)(opt)
  This function is applied on each input document right before writing.
 :prototype-f (function)(opt)
  This function is applied on each input document first before any checking
  or preparing. Must return a namespaced map!}
```

```
@usage
(save-documents! "my_collection" [{:namespace/id "MyObjectId" ...}] {...})
```

```
@example
(save-documents! "my_collection" [{:namespace/id "MyObjectId" ...}] {...})
=>
[{:namespace/id "MyObjectId" ...}]
```

```
@return (namespaced maps in vector)
[{:namespace/id (string)}]
```

<details>
<summary>Source code</summary>

```
(defn save-documents!
  ([collection-path documents]
   (save-documents! collection-path documents {}))

  ([collection-path documents options]
   (vector/->items documents #(save-document! collection-path % options))))
```

</details>

<details>
<summary>Require</summary>

```
(ns my-namespace (:require [mongo-db.api :refer [save-documents!]]))

(mongo-db.api/save-documents! ...)
(save-documents!              ...)
```

</details>

---

### update-document!

```
@description
Updates a document in a collection found by the given query.
You can apply custom functions for preparing and prototyping the document.
Returns a boolean indicating whether the update was successful.
```

```
@param (string) collection-path
@param (map) query
{:namespace/id (string)(opt)}
@param (map or namespaced map) document
No need to be a namespaced map if using a prototype function that converts it
into a namespaced form!
@param (map)(opt) options
{:prepare-f (function)(opt)
  This function is applied on the input document right before writing.
 :prototype-f (function)(opt)
  This function is applied on the input document first before any checking
  or preparing. Must return a namespaced map!}
```

```
@usage
(update-document! "my_collection" {:namespace/score 100} {:namespace/score 0} {...})
```

```
@usage
(update-document! "my_collection" {:$or [{...} {...}]} {:namespace/score 0} {...})
```

```
@usage
(update-document! "my_collection" {:$or [{...} {...}]} {:$inc {:namespace/score 0}} {...})
```

```
@return (boolean)
```

<details>
<summary>Source code</summary>

```
(defn update-document!
  ([collection-path query document]
   (update-document! collection-path query document {}))

  ([collection-path query document options]
   (boolean (if-let [document (as-> document % (actions.prototyping/update-input collection-path % options)
                                               (actions.checking/update-input %)
                                               (actions.preparing/update-input collection-path % options)
                                               (actions.adaptation/update-input %))]
                    (if-let [query (-> query reader.checking/find-query reader.adaptation/find-query)]
                            (let [result (actions.side-effects/update! collection-path query document {:multi false :upsert false})]
                                 (mrt/updated-existing? result)))))))
```

</details>

<details>
<summary>Require</summary>

```
(ns my-namespace (:require [mongo-db.api :refer [update-document!]]))

(mongo-db.api/update-document! ...)
(update-document!              ...)
```

</details>

---

### update-documents!

```
@description
Updates multiple documents in a collection found by the given query.
You can apply custom functions for preparing and prototyping each document.
Returns a boolean indicating whether the update was successful.
```

```
@param (string) collection-path
@param (map) query
{:namespace/id (string)(opt)}
@param (namespaced map) document
No need to be a namespaced map if using a prototype function that converts it
into a namespaced form!
@param (map)(opt) options
{:prepare-f (function)(opt)
  This function is applied on each input document right before writing.
 :prototype-f (function)(opt)
  This function is applied on each input document first before any checking
  or preparing. Must return a namespaced map!}
```

```
@usage
(update-documents! "my_collection" {:namespace/score 100} {:namespace/score 0} {...})
```

```
@usage
(update-documents! "my_collection" {:$or [{...} {...}]} {:namespace/score 0} {...})
```

```
@usage
(update-documents! "my_collection" {:$or [{...} {...}]} {:$inc {:namespace/score 0}} {...})
```

```
@return (boolean)
```

<details>
<summary>Source code</summary>

```
(defn update-documents!
  ([collection-path query document]
   (update-documents! collection-path query document {}))

  ([collection-path query document options]
   (boolean (if-let [document (as-> document % (actions.prototyping/update-input collection-path % options)
                                               (actions.checking/update-input %)
                                               (actions.preparing/update-input collection-path % options)
                                               (actions.adaptation/update-input %))]
                    (if-let [query (-> query reader.checking/find-query reader.adaptation/find-query)]
                            (let [result (actions.side-effects/update! collection-path query document {:multi true :upsert false})]
                                 (mrt/updated-existing? result)))))))
```

</details>

<details>
<summary>Require</summary>

```
(ns my-namespace (:require [mongo-db.api :refer [update-documents!]]))

(mongo-db.api/update-documents! ...)
(update-documents!              ...)
```

</details>

---

### upsert-document!

```
@description
Updates or inserts a document in or into a collection found by the given query.
You can apply custom functions for preparing and prototyping the document.
Returns a boolean indicating whether the updating/inserting was successful.
```

```
@param (string) collection-path
@param (map) query
@param (map or namespaced map) document
No need to be a namespaced map if using a prototype function that converts it
into a namespaced form!
@param (map)(opt) options
{:ordered? (boolean)(opt)
  Set to TRUE when upserting a document into an ordered collection!
  Default: false
 :prepare-f (function)(opt)
  This function is applied on the input document right before writing.
 :prototype-f (function)(opt)
  This function is applied on the input document first before any checking
  or preparing. Must return a namespaced map!}
```

```
@usage
(upsert-document! "my_collection" {:namespace/score 100} {:namespace/score 0} {...})
```

```
@usage
(upsert-document! "my_collection" {:$or [{...} {...}]} {:namespace/score 0} {...})
```

```
@usage
(upsert-document! "my_collection" {:$or [{...} {...}]} {:$inc {:namespace/score 0}} {...})
```

```
@return (boolean)
```

<details>
<summary>Source code</summary>

```
(defn upsert-document!
  ([collection-path query document]
   (upsert-document! collection-path query document {}))

  ([collection-path query document options]
   (boolean (if-let [document (as-> document % (actions.prototyping/upsert-input collection-path % options)
                                               (actions.checking/upsert-input %)
                                               (actions.preparing/upsert-input collection-path % options)
                                               (actions.adaptation/upsert-input %))]
                    (if-let [query (-> query reader.checking/find-query reader.adaptation/find-query)]
                            (let [result (actions.side-effects/upsert! collection-path query document {:multi false})]
                                 (mrt/acknowledged? result)))))))
```

</details>

<details>
<summary>Require</summary>

```
(ns my-namespace (:require [mongo-db.api :refer [upsert-document!]]))

(mongo-db.api/upsert-document! ...)
(upsert-document!              ...)
```

</details>

---

### upsert-documents!

```
@description
Updates or inserts multiple documents in or into a collection found by the
given query.
You can apply custom functions for preparing and prototyping each document.
Returns a boolean indicating whether the updating/inserting was successful.
```

```
@param (string) collection-path
@param (map) query
@param (namespaced map) document
No need to be a namespaced map if using a prototype function that converts it
into a namespaced form!
@param (map)(opt) options
{:ordered? (boolean)(opt)
  Set to TRUE when upserting documents into an ordered collection!
 :prepare-f (function)(opt)
  This function is applied on each input document right before writing.
 :prototype-f (function)(opt)
  This function is applied on each input document first before any checking
  or preparing. Must return a namespaced map!}
```

```
@usage
(upsert-documents! "my_collection" {:namespace/score 100} {:namespace/score 0} {...})
```

```
@usage
(upsert-documents! "my_collection" {:$or [{...} {...}]} {:namespace/score 0} {...})
```

```
@usage
(upsert-documents! "my_collection" {:$or [{...} {...}]} {:$inc {:namespace/score 0}} {...})
```

```
@return (boolean)
```

<details>
<summary>Source code</summary>

```
(defn upsert-documents!
  ([collection-path query document]
   (upsert-documents! collection-path query document {}))

  ([collection-path query document options]
   (boolean (if-let [document (as-> document % (actions.prototyping/upsert-input collection-path % options)
                                               (actions.checking/upsert-input %)
                                               (actions.preparing/upsert-input collection-path % options)
                                               (actions.adaptation/upsert-input %))]
                    (if-let [query (-> query reader.checking/find-query reader.adaptation/find-query)]
                            (let [result (actions.side-effects/upsert! collection-path query document {:multi true})]
                                 (mrt/acknowledged? result)))))))
```

</details>

<details>
<summary>Require</summary>

```
(ns my-namespace (:require [mongo-db.api :refer [upsert-documents!]]))

(mongo-db.api/upsert-documents! ...)
(upsert-documents!              ...)
```

</details>

---

<sub>This documentation is generated with the [clj-docs-generator](https://github.com/bithandshake/clj-docs-generator) engine.</sub>


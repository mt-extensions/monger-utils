
# mongo-db.api Clojure namespace

##### [README](../../../README.md) > [DOCUMENTATION](../../COVER.md) > mongo-db.api

### Index

- [apply-on-collection!](#apply-on-collection)

- [apply-on-document!](#apply-on-document)

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

### apply-on-collection!

```
@param (string) collection-name
@param (function) f
@param (map)(opt) options
{:postpare-f (function)(opt)
 :prepare-f (function)(opt)}
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
  ([collection-name f]
   (apply-on-collection! collection-name f {}))

  ([collection-name f options]
   (if-let [collection (reader.engine/get-collection collection-name)]
           (letfn [(fi [result document]
                       (if-let [document (f document)]
                               (let [document (save-document! collection-name document options)]
                                    (conj result document))
                               (return result)))]
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
@param (string) collection-name
@param (string) document-id
@param (function) f
@param (map)(opt) options
{:postpare-f (function)(opt)
 :prepare-f (function)(opt)}
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
  ([collection-name document-id f]
   (apply-on-document! collection-name document-id f {}))

  ([collection-name document-id f options]
   (if-let [document (reader.engine/get-document-by-id collection-name document-id)]
           (if-let [document (actions.preparing/apply-input collection-name document options)]
                   (if-let [document (f document)]
                           (if-let [document (actions.postparing/apply-input collection-name document options)]
                                   (if-let [document (actions.adaptation/save-input document)]
                                           (let [result (actions.helpers/save-and-return! collection-name document)]
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

### collection-empty?

```
@param (string) collection-name
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
  [collection-name]
  (= 0 (reader.helpers/count-documents collection-name)))
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
@usage
(connected?)
```

```
@return (boolean)
```

<details>
<summary>Source code</summary>

```
(defn connected?
  []
  (core.helpers/command {:ping 1 :warn? false}))
```

</details>

<details>
<summary>Require</summary>

```
(ns my-namespace (:require [mongo-db.api :refer [connected?]]))

(mongo-db.api/connected?)
(connected?)
```

</details>

---

### count-documents-by-pipeline

```
@param (string) collection-name
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
  ([collection-name pipeline]
   (count-documents-by-pipeline collection-name pipeline {}))

  ([collection-name pipeline options]
   (if-let [documents (aggregation.engine/process collection-name pipeline options)]
           (count  documents)
           (return 0))))
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
  (cond-> [] field-pattern (conj {"$addFields"      (add-fields-query field-pattern)})
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
@param (string) collection-name
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
  [collection-name document-id]
  (boolean (if-let [document-id (reader.adaptation/document-id-input document-id)]
                   (reader.helpers/find-map-by-id collection-name document-id))))
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
@param (string) collection-name
@param (string) document-id
@param (map)(opt) options
{:changes (namespaced map)(opt)
 :label-key (namespaced keyword)(opt)
  A dokumentum melyik kulcsának értékéhez fűzze hozzá a "#..." kifejezést
 :ordered? (boolean)(opt)
  Default: false
 :postpare-f (function)(opt)
 :prepare-f (function)(opt)}
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
  ([collection-name document-id]
   (duplicate-document! collection-name document-id {}))

  ([collection-name document-id {:keys [ordered?] :as options}]
   (if ordered? (duplicate-ordered-document!   collection-name document-id options)
                (duplicate-unordered-document! collection-name document-id options))))
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
@param (string) collection-name
@param (strings in vector) document-ids
@param (map)(opt) options
{:label-key (namespaced keyword)(opt)
  A dokumentum melyik kulcsának értékéhez fűzze hozzá a "#..." kifejezést
 :ordered? (boolean)(opt)
  Default: false
 :prepare-f (function)(opt)}
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
  ([collection-name document-ids]
   (duplicate-documents! collection-name document-ids {}))

  ([collection-name document-ids options]
   (vector/->items document-ids #(duplicate-document! collection-name % options))))
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
@usage
(mongo-db/generate-id)
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
@param (string) collection-name
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
  [collection-name]
  (reader.helpers/count-documents collection-name))
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
@param (string) collection-name
@param (map)(opt) options
{:projection (namespaced map)(opt)
 :prototype-f (function)(opt)}
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
  ([collection-name]
   (if-let [collection (reader.helpers/find-maps collection-name {})]
           (vector/->items collection #(reader.adaptation/find-output %))))

  ([collection-name {:keys [projection] :as options}]
   (if-let [projection (reader.adaptation/find-projection projection)]
           (if-let [collection (reader.helpers/find-maps collection-name {} projection)]
                   (letfn [(f [document] (as-> document % (reader.adaptation/find-output  %)
                                                          (reader.prototyping/find-output % options)))]
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
@usage
(get-collection-names)
```

```
@return (strings in vector)
```

<details>
<summary>Source code</summary>

```
(defn get-collection-names
  []
  (let [database @(r/subscribe [:mongo-db/get-connection])]
       (-> database mdb/get-collection-names vec)))
```

</details>

<details>
<summary>Require</summary>

```
(ns my-namespace (:require [mongo-db.api :refer [get-collection-names]]))

(mongo-db.api/get-collection-names)
(get-collection-names)
```

</details>

---

### get-collection-namespace

```
@param (string) collection-name
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
  [collection-name]
  (let [collection (reader.helpers/find-maps collection-name {})]
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
@param (string) collection-name
@param (string) document-id
@param (map)(opt) options
{:projection (namespaced map)(opt)
 :prototype-f (function)(opt)}
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
  ([collection-name document-id]
   (if-let [document-id (reader.adaptation/document-id-input document-id)]
           (if-let [document (reader.helpers/find-map-by-id collection-name document-id)]
                   (reader.adaptation/find-output document))))

  ([collection-name document-id {:keys [projection] :as options}]
   (if-let [document-id (reader.adaptation/document-id-input document-id)]
           (if-let [projection (reader.adaptation/find-projection projection)]
                   (if-let [document (reader.helpers/find-map-by-id collection-name document-id projection)]
                           (as-> document % (reader.adaptation/find-output  %)
                                            (reader.prototyping/find-output % options)))))))
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
@param (string) collection-name
@param (map) query
@param (map)(opt) options
{:projection (namespaced map)(opt)
 :prototype-f (function)(opt)}
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
  ([collection-name query]
   (if-let [query (-> query reader.checking/find-query reader.adaptation/find-query)]
           (if-let [document (reader.helpers/find-one-as-map collection-name query)]
                   (reader.adaptation/find-output document))))

  ([collection-name query {:keys [projection] :as options}]
   (if-let [query (-> query reader.checking/find-query reader.adaptation/find-query)]
           (if-let [projection (reader.adaptation/find-projection projection)]
                   (if-let [document (reader.helpers/find-one-as-map collection-name query projection)]
                           (as-> document % (reader.adaptation/find-output  %)
                                            (reader.prototyping/find-output % options)))))))
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
@param (string) collection-name
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
  [collection-name query]
  (if-let [query (-> query reader.checking/find-query reader.adaptation/find-query)]
          (reader.helpers/count-documents-by-query collection-name query)))
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
@param (string) collection-name
@param (maps in vector) pipeline
@param (map)(opt) options
{:locale (string)(opt)
  Default: "en"
  https://www.mongodb.com/docs/manual/reference/collation-locales-defaults
 :prototype-f (function)(opt)}
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
  ([collection-name pipeline]
   (get-documents-by-pipeline collection-name pipeline {}))

  ([collection-name pipeline options]
   (if-let [documents (aggregation.engine/process collection-name pipeline options)]
           (letfn [(f [document] (as-> document % (reader.adaptation/find-output  %)
                                                  (reader.prototyping/find-output % options)))]
                  (vector/->items documents f))
           (return []))))
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
@param (string) collection-name
@param (map) query
@param (map)(opt) options
{:projection (namespaced map)(opt)
 :prototype-f (function)(opt)}
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
  ([collection-name query]
   (if-let [query (-> query reader.checking/find-query reader.adaptation/find-query)]
           (if-let [documents (reader.helpers/find-maps collection-name query)]
                   (vector/->items documents #(reader.adaptation/find-output %)))))

  ([collection-name query {:keys [projection] :as options}]
   (if-let [query (-> query reader.checking/find-query reader.adaptation/find-query)]
           (if-let [projection (reader.adaptation/find-projection projection)]
                   (if-let [documents (reader.helpers/find-maps collection-name query projection)]
                           (letfn [(f [document] (as-> document % (reader.adaptation/find-output  %)
                                                                  (reader.prototyping/find-output % options)))]
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
@param (string) collection-name
@param (map)(opt) options
{:prototype-f (function)(opt)}
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
  [collection-name]
  (let [collection (get-collection collection-name)]
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
@param (string) collection-name
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
  [collection-name]
  (let [collection (get-collection collection-name)]
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
  (cond-> [] field-pattern (conj {"$addFields"      (add-fields-query field-pattern)})
             :match        (conj {"$match" {"$and" [(filter-query     filter-pattern)
                                                    (search-query     search-pattern)]}})
             sort-pattern  (conj {"$sort"           (sort-query       sort-pattern)})
             unset-pattern (conj {"$unset"          (unset-query      unset-pattern)})
             skip          (conj {"$skip"           (param            skip)})
             max-count     (conj {"$limit"          (param            max-count)})))
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
@param (string) collection-name
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
  ([collection-name specified-keys]
   (get-specified-values collection-name specified-keys some?))

  ([collection-name specified-keys test-f]
   (letfn [(f [result document]
              (letfn [(f [result k]
                         (let [v (get document k)]
                              (if (test-f v)
                                  (update result k vector/conj-item-once v)
                                  (return result))))]
                     (reduce f result specified-keys)))]
          (let [collection (get-collection collection-name)]
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
@param (string) collection-name
@param (namespaced map) document
{:namespace/id (string)(opt)}
@param (map)(opt) options
{:ordered? (boolean)(opt)
  Default: false
 :prepare-f (function)(opt)}
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
  ([collection-name document]
   (insert-document! collection-name document {}))

  ([collection-name document options]
   (if-let [document (as-> document % (actions.checking/insert-input %)
                                      (actions.preparing/insert-input collection-name % options)
                                      (actions.adaptation/insert-input %))]
           (if-let [result (actions.helpers/insert-and-return! collection-name document)]
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
@param (string) collection-name
@param (namespaced maps in vector) documents
[{:namespace/id (string)(opt)}]
@param (map)(opt) options
{:ordered? (boolean)(opt)
  Default: false
 :prepare-f (function)(opt)}
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
  ([collection-name documents]
   (insert-documents! collection-name documents {}))

  ([collection-name documents options]
   (vector/->items documents #(insert-document! collection-name % options))))
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

### remove-all-documents!

```
@param (string) collection-name
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
  [collection-name]
  (actions.helpers/drop! collection-name))
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
@param (string) collection-name
@param (string) document-id
@param (map)(opt) options
{:ordered? (boolean)
  Default: false}
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
  ([collection-name document-id]
   (remove-document! collection-name document-id {}))

  ([collection-name document-id {:keys [ordered?] :as options}]
   (if ordered? (remove-ordered-document!   collection-name document-id options)
                (remove-unordered-document! collection-name document-id options))))
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
@param (string) collection-name
@param (strings in vector) document-ids
@param (map)(opt) options
{:ordered? (boolean)
  Default: false}
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
  ([collection-name document-ids]
   (remove-documents! collection-name document-ids {}))

  ([collection-name document-ids options]
   (vector/->items document-ids #(remove-document! collection-name % options))))
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
@param (string) collection-name
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
  [collection-name document-order]
  (let [namespace (reader.engine/get-collection-namespace collection-name)
        order-key (keyword/add-namespace namespace :order)]
       (letfn [(f [[document-id document-dex]]
                  (if-let [document-id (actions.adaptation/document-id-input document-id)]
                          (let [result (actions.helpers/update! collection-name {:_id document-id}
                                                                {"$set" {order-key document-dex}})]
                               (if (mrt/acknowledged? result)
                                   (return [document-id document-dex])))))]
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
@param (string) collection-name
@param (namespaced map) document
{:namespace/id (string)(opt)}
@param (map)(opt) options
{:ordered? (boolean)(opt)
  Default: false
 :prepare-f (function)(opt)}
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
  ([collection-name document]
   (save-document! collection-name document {}))

  ([collection-name document options]
   (if-let [document (as-> document % (actions.checking/save-input %)
                                      (actions.preparing/save-input collection-name % options)
                                      (actions.adaptation/save-input %))]
           (if-let [result (actions.helpers/save-and-return! collection-name document)]
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
@param (string) collection-name
@param (namespaced maps in vector) documents
[{:namespace/id (string)(opt)}]
@param (map)(opt) options
{:ordered? (boolean)(opt)
  Default: false
 :prepare-f (function)(opt)}
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
  ([collection-name documents]
   (save-documents! collection-name documents {}))

  ([collection-name documents options]
   (vector/->items documents #(save-document! collection-name % options))))
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
@param (string) collection-name
@param (map) query
{:namespace/id (string)(opt)}
@param (map or namespaced map) document
@param (map)(opt) options
{:prepare-f (function)(opt)}
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
  ([collection-name query document]
   (update-document! collection-name query document {}))

  ([collection-name query document options]
   (boolean (if-let [document (as-> document % (actions.checking/update-input %)
                                               (actions.preparing/update-input collection-name % options)
                                               (actions.adaptation/update-input %))]
                    (if-let [query (-> query reader.checking/find-query reader.adaptation/find-query)]
                            (let [result (actions.helpers/update! collection-name query document {:multi false :upsert false})]
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
@param (string) collection-name
@param (map) query
{:namespace/id (string)(opt)}
@param (namespaced map) document
@param (map)(opt) options
{:prepare-f (function)(opt)}
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
  ([collection-name query document]
   (update-documents! collection-name query document {}))

  ([collection-name query document options]
   (boolean (if-let [document (as-> document % (actions.checking/update-input %)
                                               (actions.preparing/update-input collection-name % options)
                                               (actions.adaptation/update-input %))]
                    (if-let [query (-> query reader.checking/find-query reader.adaptation/find-query)]
                            (let [result (actions.helpers/update! collection-name query document {:multi true :upsert false})]
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
@param (string) collection-name
@param (map) query
@param (map or namespaced map) document
@param (map)(opt) options
{:ordered? (boolean)(opt)
  Default: false
 :prepare-f (function)(opt)}
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
  ([collection-name query document]
   (upsert-document! collection-name query document {}))

  ([collection-name query document options]
   (boolean (if-let [document (as-> document % (actions.checking/upsert-input %)
                                               (actions.preparing/upsert-input collection-name % options)
                                               (actions.adaptation/upsert-input %))]
                    (if-let [query (-> query reader.checking/find-query reader.adaptation/find-query)]
                            (let [result (actions.helpers/upsert! collection-name query document {:multi false})]
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
@param (string) collection-name
@param (map) query
@param (namespaced map) document
@param (map)(opt) options
{:ordered? (boolean)(opt)
 :prepare-f (function)(opt)}
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
  ([collection-name query document]
   (upsert-documents! collection-name query document {}))

  ([collection-name query document options]
   (boolean (if-let [document (as-> document % (actions.checking/upsert-input %)
                                               (actions.preparing/upsert-input collection-name % options)
                                               (actions.adaptation/upsert-input %))]
                    (if-let [query (-> query reader.checking/find-query reader.adaptation/find-query)]
                            (let [result (actions.helpers/upsert! collection-name query document {:multi true})]
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

This documentation is generated by the [docs-api](https://github.com/bithandshake/docs-api) engine


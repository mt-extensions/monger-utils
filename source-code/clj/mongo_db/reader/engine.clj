
(ns mongo-db.reader.engine
    (:require monger.joda-time
              [candy.api                   :refer [return]]
              [map.api                     :as map]
              [monger.db                   :as mdb]
              [mongo-db.aggregation.engine :as aggregation.engine]
              [mongo-db.reader.adaptation  :as reader.adaptation]
              [mongo-db.reader.checking    :as reader.checking]
              [mongo-db.reader.helpers     :as reader.helpers]
              [mongo-db.reader.prototyping :as reader.prototyping]
              [re-frame.api                :as r]
              [vector.api                  :as vector]))

;; -- Collection functions ----------------------------------------------------
;; ----------------------------------------------------------------------------

(defn get-collection-names
  ; @usage
  ; (get-collection-names)
  ;
  ; @return (strings in vector)
  []
  (let [database @(r/subscribe [:mongo-db/get-connection])]
       (-> database mdb/get-collection-names vec)))

(defn get-collection-namespace
  ; @param (string) collection-name
  ;
  ; @usage
  ; (get-collection-namespace "my_collection")
  ;
  ; @return (keyword)
  [collection-name]
  (let [collection (reader.helpers/find-maps collection-name {})]
       (-> collection first map/get-namespace)))

(defn get-all-document-count
  ; @param (string) collection-name
  ;
  ; @usage
  ; (get-all-document-count "my_collection")
  ;
  ; @return (integer)
  [collection-name]
  (reader.helpers/count-documents collection-name))

(defn collection-empty?
  ; @param (string) collection-name
  ;
  ; @usage
  ; (collection-empty? "my_collection")
  ;
  ; @return (boolean)
  [collection-name]
  (= 0 (reader.helpers/count-documents collection-name)))

(defn get-document-count-by-query
  ; @param (string) collection-name
  ; @param (map) query
  ;
  ; @usage
  ; (get-document-count-by-query "my_collection" {:namespace/my-keyword :my-value})
  ;
  ; @usage
  ; (get-document-count-by-query "my_collection" {:$or [{...} {...}]})
  ;
  ; @usage
  ; (get-document-count-by-query "my_collection" {:namespace/my-keyword  :my-value}
  ;                                               :namespace/your-string "Your value"})
  ;
  ; @return (integer)
  [collection-name query]
  (if-let [query (-> query reader.checking/find-query reader.adaptation/find-query)]
          (reader.helpers/count-documents-by-query collection-name query)))

(defn get-collection
  ; @param (string) collection-name
  ; @param (map)(opt) options
  ; {:projection (namespaced map)(opt)
  ;  :prototype-f (function)(opt)}
  ;
  ; @usage
  ; (get-collection "my_collection")
  ;
  ; @usage
  ; (get-collection "my_collection" {...})
  ;
  ; @example
  ; (get-collection "my_collection"
  ;                 {:projection {:namespace/id          1
  ;                               :namespace/your-string 1}})
  ; =>
  ; [{:namespace/id          "MyObjectId"
  ;   :namespace/your-string "Your value"}]
  ;
  ; @example
  ; (get-collection "my_collection"
  ;                 {:prototype-f (fn [document] (assoc document :namespace/my-string "My value"))}})
  ; =>
  ; [{:namespace/id        "MyObjectId"
  ;   :namespace/my-string "My value"}]
  ;
  ; @return (maps in vector)
  ; [{:namespace/id (string)}]
  ([collection-name]
   (if-let [collection (reader.helpers/find-maps collection-name {})]
           (vector/->items collection #(reader.adaptation/find-output %))))

  ([collection-name {:keys [projection] :as options}]
   (if-let [projection (reader.adaptation/find-projection projection)]
           (if-let [collection (reader.helpers/find-maps collection-name {} projection)]
                   (letfn [(f [document] (as-> document % (reader.adaptation/find-output  %)
                                                          (reader.prototyping/find-output % options)))]
                          (vector/->items collection f))))))

(defn get-documents-by-query
  ; @param (string) collection-name
  ; @param (map) query
  ; @param (map)(opt) options
  ; {:projection (namespaced map)(opt)
  ;  :prototype-f (function)(opt)}
  ;
  ; @usage
  ; (get-documents-by-query "my_collection" {:namespace/my-keyword :my-value})
  ;
  ; @usage
  ; (get-documents-by-query "my_collection" {:$or [{...} {...}]})
  ;
  ; @example
  ; (get-documents-by-query "my_collection" {:namespace/my-keyword :my-value}
  ;                         {:projection {:namespace/id          1
  ;                                       :namespace/your-string 1}})
  ; =>
  ; [{:namespace/id          "MyObjectId"
  ;   :namespace/my-keyword  :my-value
  ;   :namespace/your-string "Your value"}]
  ;
  ; @example
  ; (get-documents-by-query "my_collection" {:namespace/my-keyword :my-value}
  ;                         {:prototype-f (fn [document] (assoc document :namespace/my-string "My value"))})
  ; =>
  ; [{:namespace/id           "MyObjectId"
  ;   :namespace/my-string    "My value"
  ;   :namespace/your-keyword :my-value}]
  ;
  ; @return (namespaced maps in vector)
  ; [{:namespace/id (string)}]
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

;; -- Document functions ------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn get-document-by-query
  ; @param (string) collection-name
  ; @param (map) query
  ; @param (map)(opt) options
  ; {:projection (namespaced map)(opt)
  ;  :prototype-f (function)(opt)}
  ;
  ; @usage
  ; (get-document-by-query "my_collection" {:namespace/my-keyword :my-value})
  ;
  ; @usage
  ; (get-document-by-query "my_collection" {:namespace/my-keyword :my-value} {:prototype-f :namespace/my-keyword})
  ;
  ; @usage
  ; (get-document-by-query "my_collection" {:$or [{...} {...}]})
  ;
  ; @example
  ; (get-document-by-query "my_collection" {:namespace/my-keyword :my-value}
  ;                        {:projection {:namespace/id          1
  ;                                      :namespace/your-string 1}})
  ; =>
  ; {:namespace/id          "MyObjectId"
  ;  :namespace/your-string "Your value"}
  ;
  ; @example
  ; (get-document-by-query "my_collection" {:namespace/my-keyword :my-value}
  ;                        {:prototype-f (fn [document] (assoc document :namespace/my-string "My value"))})
  ; =>
  ; {:namespace/id         "MyObjectId"
  ;  :namespace/my-keyword :my-value
  ;  :namespace/my-string  "My value"}
  ;
  ; @return (namespaced map)
  ; {:namespace/id (string)}
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

(defn get-document-by-id
  ; @param (string) collection-name
  ; @param (string) document-id
  ; @param (map)(opt) options
  ; {:projection (namespaced map)(opt)
  ;  :prototype-f (function)(opt)}
  ;
  ; @usage
  ; (get-document-by-id "my_collection" "MyObjectId")
  ;
  ; @usage
  ; (get-document-by-id "my_collection" "MyObjectId" {:prototype-f :namespace/my-keyword})
  ;
  ; @example
  ; (get-document-by-id "my_collection" "MyObjectId"
  ;                     {:projection {:namespace/id          1
  ;                                   :namespace/your-string 1}})
  ; =>
  ; {:namespace/id          "MyObjectId"
  ;  :namespace/your-string "Your value"}
  ;
  ; @example
  ; (get-document-by-id "my_collection" "MyObjectId"
  ;                     {:prototype-f (fn [document] (assoc document :namespace/my-string "My value"))})
  ; =>
  ; {:namespace/id        "MyObjectId"
  ;  :namespace/my-string "My value"}
  ;
  ; @return (namespaced map)
  ; {:namespace/id (string)}
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

(defn get-first-document
  ; @param (string) collection-name
  ;
  ; @usage
  ; (get-first-document "my_collection")
  ;
  ; @return (namespaced map)
  [collection-name]
  (let [collection (get-collection collection-name)]
       (first collection)))

(defn get-last-document
  ; @param (string) collection-name
  ;
  ; @usage
  ; (get-last-document "my_collection")
  ;
  ; @return (namespaced map)
  [collection-name]
  (let [collection (get-collection collection-name)]
       (last collection)))

(defn document-exists?
  ; @param (string) collection-name
  ; @param (string) document-id
  ;
  ; @usage
  ; (document-exists? "my_collection" "MyObjectId")
  ;
  ; @return (boolean)
  [collection-name document-id]
  (boolean (if-let [document-id (reader.adaptation/document-id-input document-id)]
                   (reader.helpers/find-map-by-id collection-name document-id))))

;; -- Advanced DB functions ---------------------------------------------------
;; ----------------------------------------------------------------------------

(defn get-documents-by-pipeline
  ; @param (string) collection-name
  ; @param (maps in vector) pipeline
  ; @param (map)(opt) options
  ; {:locale (string)(opt)
  ;   Default: "en"
  ;   https://www.mongodb.com/docs/manual/reference/collation-locales-defaults}
  ;
  ; @usage
  ; (get-documents-by-pipeline "my_collection" [...])
  ;
  ; @usage
  ; (get-documents-by-pipeline "my_collection" (get-pipeline {...}))
  ;
  ; @usage
  ; (get-documents-by-pipeline "my_collection" [...] {:locale "en"})
  ;
  ; @return (namespaced maps in vector)
  ([collection-name pipeline]
   (get-documents-by-pipeline collection-name pipeline {}))

  ([collection-name pipeline options]
   (if-let [documents (aggregation.engine/process collection-name pipeline options)]
           (vector/->items documents #(reader.adaptation/find-output %))
           (return []))))

(defn count-documents-by-pipeline
  ; @param (string) collection-name
  ; @param (maps in vector) pipeline
  ; @param (map)(opt) options
  ; {:locale (string)(opt)
  ;   Default: "en"
  ;   https://www.mongodb.com/docs/manual/reference/collation-locales-defaults}
  ;
  ; @usage
  ; (count-documents-by-pipeline "my_collection" [...])
  ;
  ; @usage
  ; (count-documents-by-pipeline "my_collection" (count-pipeline {...}))
  ;
  ; @return (integer)
  ([collection-name pipeline]
   (count-documents-by-pipeline collection-name pipeline {}))

  ([collection-name pipeline options]
   (if-let [documents (aggregation.engine/process collection-name pipeline options)]
           (count  documents)
           (return 0))))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn get-specified-values
  ; @param (string) collection-name
  ; @param (keywords in vector) specified-keys
  ; @param (function)(opt) test-f
  ; Default: some?
  ;
  ; @example
  ; (get-specified-values "my_collection" [:my-key :your-key] string?)
  ; =>
  ; {:my-key   ["..." "..."]
  ;  :your-key ["..." "..."]}
  ;
  ; @return (map)
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

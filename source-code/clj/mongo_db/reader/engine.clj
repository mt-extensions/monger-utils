
(ns mongo-db.reader.engine
    (:require monger.joda-time
              [map.api                     :as map]
              [monger.db                   :as mdb]
              [mongo-db.aggregation.engine :as aggregation.engine]
              [mongo-db.connection.state   :as connection.state]
              [mongo-db.connection.utils   :as connection.utils]
              [mongo-db.core.errors        :as core.errors]
              [mongo-db.reader.adaptation  :as reader.adaptation]
              [mongo-db.reader.checking    :as reader.checking]
              [mongo-db.reader.env         :as reader.env]
              [mongo-db.reader.prototyping :as reader.prototyping]
              [vector.api                  :as vector]))

;; -- Collection functions ----------------------------------------------------
;; ----------------------------------------------------------------------------

(defn get-collection-names
  ; @param (string)(opt) database-name
  ;
  ; @usage
  ; (get-collection-names)
  ;
  ; @usage
  ; (get-collection-names "my-database")
  ;
  ; @return (strings in vector)
  ([]
   (let [database-name (connection.utils/default-database-name)]
        (get-collection-names database-name)))

  ([database-name]
   (if-let [database-reference (get @connection.state/REFERENCES database-name)]
           (-> database-reference mdb/get-collection-names vec)
           (try (throw (Exception. core.errors/NO-DATABASE-REFERENCE-FOUND-ERROR))
                (catch Exception e (println (str e "\n" {:database-name database-name})))))))

(defn get-collection-namespace
  ; @param (string) collection-path
  ;
  ; @usage
  ; (get-collection-namespace "my_collection")
  ;
  ; @return (keyword)
  [collection-path]
  (let [collection (reader.env/find-maps collection-path {})]
       (-> collection first map/get-namespace)))

(defn get-all-document-count
  ; @param (string) collection-path
  ;
  ; @usage
  ; (get-all-document-count "my_collection")
  ;
  ; @return (integer)
  [collection-path]
  (reader.env/count-documents collection-path))

(defn collection-empty?
  ; @param (string) collection-path
  ;
  ; @usage
  ; (collection-empty? "my_collection")
  ;
  ; @return (boolean)
  [collection-path]
  (= 0 (reader.env/count-documents collection-path)))

(defn get-document-count-by-query
  ; @param (string) collection-path
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
  [collection-path query]
  (if-let [query (-> query reader.checking/find-query reader.adaptation/find-query)]
          (reader.env/count-documents-by-query collection-path query)))

(defn get-collection
  ; @param (string) collection-path
  ; @param (map)(opt) options
  ; {:projection (namespaced map)(opt)
  ;  :prototype-f (function)(opt)
  ;   This function is applied on each output document.}
  ;
  ; @usage
  ; (get-collection "my_collection")
  ;
  ; @usage
  ; (get-collection "my_collection" {...})
  ;
  ; @example
  ; (get-collection "my_collection" {:projection {:namespace/id          1
  ;                                               :namespace/your-string 1}})
  ; =>
  ; [{:namespace/id          "MyObjectId"
  ;   :namespace/your-string "Your value"}]
  ;
  ; @example
  ; (get-collection "my_collection" {:prototype-f :namespace/my-string}})
  ; =>
  ; ["MY value" "Your value"]
  ;
  ; @return (namespaced maps or * in vector)
  ; [{:namespace/id (string)}]
  ([collection-path]
   (if-let [collection (reader.env/find-maps collection-path {})]
           (vector/->items collection #(reader.adaptation/find-output %))))

  ([collection-path {:keys [projection] :as options}]
   (if-let [projection (reader.adaptation/find-projection projection)]
           (if-let [collection (reader.env/find-maps collection-path {} projection)]
                   (letfn [(f [document] (as-> document % (reader.adaptation/find-output %)
                                                          (reader.prototyping/find-output collection-path % options)))]
                          (vector/->items collection f))))))

(defn get-documents-by-query
  ; @param (string) collection-path
  ; @param (map) query
  ; @param (map)(opt) options
  ; {:projection (namespaced map)(opt)
  ;  :prototype-f (function)(opt)
  ;   This function is applied on each output document.}
  ;
  ; @usage
  ; (get-documents-by-query "my_collection" {:namespace/my-keyword :my-value})
  ;
  ; @usage
  ; (get-documents-by-query "my_collection" {:$or [{...} {...}]})
  ;
  ; @example
  ; (get-documents-by-query "my_collection" {:namespace/my-keyword :my-value}
  ;                                         {:projection {:namespace/id          1
  ;                                                       :namespace/your-string 1}})
  ; =>
  ; [{:namespace/id          "MyObjectId"
  ;   :namespace/my-keyword  :my-value
  ;   :namespace/your-string "Your value"}]
  ;
  ; @example
  ; (get-documents-by-query "my_collection" {:namespace/my-keyword :my-value}
  ;                                         {:prototype-f :namespace/my-string})
  ; =>
  ; ["My value" "Your value"]
  ;
  ; @return (namespaced maps or * in vector)
  ; [{:namespace/id (string)}]
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

;; -- Document functions ------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn get-document-by-query
  ; @param (string) collection-path
  ; @param (map) query
  ; @param (map)(opt) options
  ; {:projection (namespaced map)(opt)
  ;  :prototype-f (function)(opt)
  ;   This function is applied on the output document.}
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
  ;                                        {:projection {:namespace/id          1
  ;                                                      :namespace/your-string 1}})
  ; =>
  ; {:namespace/id          "MyObjectId"
  ;  :namespace/your-string "Your value"}
  ;
  ; @example
  ; (get-document-by-query "my_collection" {:namespace/my-keyword :my-value}
  ;                                        {:prototype-f :namespace/my-string})
  ; =>
  ; "My value"
  ;
  ; @return (namespaced map or *)
  ; {:namespace/id (string)}
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

(defn get-document-by-id
  ; @param (string) collection-path
  ; @param (string) document-id
  ; @param (map)(opt) options
  ; {:projection (namespaced map)(opt)
  ;  :prototype-f (function)(opt)
  ;   This function is applied on the output document.}
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
  ; (get-document-by-id "my_collection" "MyObjectId" {:prototype-f :namespace/my-string})
  ; =>
  ; "My value"
  ;
  ; @return (namespaced map or *)
  ; {:namespace/id (string)}
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

(defn get-first-document
  ; @param (string) collection-path
  ; @param (map)(opt) options
  ; {:prototype-f (function)(opt)
  ;   This function is applied on the output document.}
  ;
  ; @usage
  ; (get-first-document "my_collection")
  ;
  ; @usage
  ; (get-first-document "my_collection" {:prototype-f :namespace/my-string})
  ;
  ; @return (namespaced map or *)
  [collection-path]
  (let [collection (get-collection collection-path)]
       (first collection)))

(defn get-last-document
  ; @param (string) collection-path
  ;
  ; @usage
  ; (get-last-document "my_collection")
  ;
  ; @usage
  ; (get-last-document "my_collection" {:prototype-f :namespace/my-string})
  ;
  ; @return (namespaced map or *)
  [collection-path]
  (let [collection (get-collection collection-path)]
       (last collection)))

(defn document-exists?
  ; @param (string) collection-path
  ; @param (string) document-id
  ;
  ; @usage
  ; (document-exists? "my_collection" "MyObjectId")
  ;
  ; @return (boolean)
  [collection-path document-id]
  (boolean (if-let [document-id (reader.adaptation/document-id-input document-id)]
                   (reader.env/find-map-by-id collection-path document-id))))

;; -- Advanced DB functions ---------------------------------------------------
;; ----------------------------------------------------------------------------

(defn get-documents-by-pipeline
  ; @param (string) collection-path
  ; @param (maps in vector) pipeline
  ; @param (map)(opt) options
  ; {:locale (string)(opt)
  ;   Default: "en"
  ;   https://www.mongodb.com/docs/manual/reference/collation-locales-defaults
  ;  :prototype-f (function)(opt)
  ;   This function is applied on each output document.}
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
  ; @usage
  ; (get-documents-by-pipeline "my_collection" [...] {:prototype-f :namespace/my-string})
  ;
  ; @return (namespaced maps or * in vector)
  ([collection-path pipeline]
   (get-documents-by-pipeline collection-path pipeline {}))

  ([collection-path pipeline options]
   (if-let [documents (aggregation.engine/process collection-path pipeline options)]
           (letfn [(f [document] (as-> document % (reader.adaptation/find-output %)
                                                  (reader.prototyping/find-output collection-path % options)))]
                  (vector/->items documents f))
           (-> []))))

(defn count-documents-by-pipeline
  ; @param (string) collection-path
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
  ([collection-path pipeline]
   (count-documents-by-pipeline collection-path pipeline {}))

  ([collection-path pipeline options]
   (if-let [documents (aggregation.engine/process collection-path pipeline options)]
           (-> documents count)
           (-> 0))))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn get-specified-values
  ; @param (string) collection-path
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

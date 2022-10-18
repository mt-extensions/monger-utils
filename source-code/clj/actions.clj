
;; -- Namespace ---------------------------------------------------------------
;; ----------------------------------------------------------------------------

(ns mongo-db.actions
    (:require monger.joda-time
              [mid-fruits.candy    :refer [return]]
              [mid-fruits.keyword  :as keyword]
              [mid-fruits.map      :as map]
              [mid-fruits.vector   :as vector]
              [monger.collection   :as mcl]
              [monger.operators    :refer :all]
              [monger.result       :as mrt]
              [mongo-db.adaptation :as adaptation]
              [mongo-db.checking   :as checking]
              [mongo-db.engine     :as engine]
              [mongo-db.errors     :as errors]
              [mongo-db.postparing :as postparing]
              [mongo-db.preparing  :as preparing]
              [mongo-db.reader     :as reader]
              [re-frame.api        :as r]))



;; -- Error handling ----------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn- drop!
  ; WARNING! NON-PUBLIC! DO NOT USE!
  ;
  ; @param (string) collection-name
  ;
  ; @return (?)
  [collection-name]
  (let [database @(r/subscribe [:mongo-db/get-connection])]
       (try (mcl/drop database collection-name)
            (catch Exception e (println e (str e "\n" {:collection-name collection-name}))))))

(defn- insert-and-return!
  ; WARNING! NON-PUBLIC! DO NOT USE!
  ;
  ; @param (string) collection-name
  ; @param (map) document
  ;  {"_id" (org.bson.types.ObjectId object)}
  ;
  ; @return (namespaced map)
  [collection-name document]
  (let [database @(r/subscribe [:mongo-db/get-connection])]
       (try (mcl/insert-and-return database collection-name document)
            (catch Exception e (println (str e "\n" {:collection-name collection-name :document document}))))))

(defn- save-and-return!
  ; WARNING! NON-PUBLIC! DO NOT USE!
  ;
  ; @param (string) collection-name
  ; @param (namespaced map) document
  ;  {"_id" (org.bson.types.ObjectId object)}
  ;
  ; @return (namespaced map)
  [collection-name document]
  (let [database @(r/subscribe [:mongo-db/get-connection])]
       (try (mcl/save-and-return database collection-name document)
            (catch Exception e (println (str e "\n" {:collection-name collection-name :document document}))))))

(defn- remove-by-id!
  ; WARNING! NON-PUBLIC! DO NOT USE!
  ;
  ; @param (string) collection-name
  ; @param (org.bson.types.ObjectId object) document-id
  ;
  ; @return (?)
  [collection-name document-id]
  (let [database @(r/subscribe [:mongo-db/get-connection])]
       (try (mcl/remove-by-id database collection-name document-id)
            (catch Exception e (println (str e "\n" {:collection-name collection-name :document-id document-id}))))))

(defn- update!
  ; WARNING! NON-PUBLIC! DO NOT USE!
  ;
  ; @param (string) collection-name
  ; @param (map) query
  ;  {"_id" (org.bson.types.ObjectId object)(opt)}
  ; @param (map) document
  ; @param (map)(opt) options
  ;  {:multi (boolean)(opt)
  ;    Default: false
  ;   :upsert (boolean)(opt)
  ;    Default: false}
  ;
  ; @return (com.mongodb.WriteResult object)
  ([collection-name query document]
   (update! collection-name query document {}))

  ([collection-name query document options]
   (let [database @(r/subscribe [:mongo-db/get-connection])]
        (try (mcl/update database collection-name query document options)
             (catch Exception e (println (str e "\n" {:collection-name collection-name :query   query
                                                      :document        document        :options options})))))))

(defn- upsert!
  ; WARNING! NON-PUBLIC! DO NOT USE!
  ;
  ; @param (string) collection-name
  ; @param (map) query
  ;  {"_id" (org.bson.types.ObjectId object)(opt)}
  ; @param (map) document
  ; @param (map)(opt) options
  ;  {:multi (boolean)(opt)
  ;    Default: false}
  ;
  ; @return (com.mongodb.WriteResult object)
  ([collection-name query document]
   (upsert! collection-name query document {}))

  ([collection-name query document options]
   (let [database @(r/subscribe [:mongo-db/get-connection])]
        (try (mcl/upsert database collection-name query document options)
             (catch Exception e (println (str e "\n" {:collection-name collection-name :query   query
                                                      :document        document        :options options})))))))



;; -- Reordering following documents ------------------------------------------
;; ----------------------------------------------------------------------------

(defn- reorder-following-documents!
  ; WARNING! NON-PUBLIC! DO NOT USE!
  ;
  ; @param (string) collection-name
  ; @param (string) document-id
  ; @param (map) options
  ;  {:operation (keyword)
  ;    :decrease, :increase}
  ;
  ; @return (namespaced map)
  [collection-name document-id {:keys [operation]}]
  ; Egy rendezett kollekcióból történő dokumentum eltávolítása a dokumentum után sorrendben
  ; következő többi dokumentum pozíciójának csökkentését teszi szükségessé.
  ;
  ; Egy rendezett kollekcióba történő dokumentum beszúrása a dokumentum után sorrendben következő
  ; többi dokumentum pozíciójának növelését teszi szükségessé.
  (if-let [document (reader/get-document-by-id collection-name document-id)]
          (let [namespace    (map/get-namespace     document)
                order-key    (keyword/add-namespace namespace :order)
                document-dex (get document order-key)
                query        {order-key {:$gt document-dex}}
                document     (case operation :increase {:$inc {order-key  1}}
                                             :decrease {:$inc {order-key -1}})]
               (if-let [query (-> query checking/update-query adaptation/update-query)]
                       (if-let [document (-> document checking/update-input adaptation/update-input)]
                               ; A sorrendben a dokumentum után következő dokumentumok sorrendbeli pozíciójának eltolása
                               (let [result (update! collection-name query document {:multi true})]
                                    (if-not (mrt/acknowledged? result)
                                            (throw (Exception. errors/REORDER-DOCUMENTS-FAILED)))))))
          (throw (Exception. errors/DOCUMENT-DOES-NOT-EXISTS-ERROR))))



;; -- Inserting document ------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn insert-document!
  ; @param (string) collection-name
  ; @param (namespaced map) document
  ;  {:namespace/id (string)(opt)}
  ; @param (map)(opt) options
  ;  {:ordered? (boolean)(opt)
  ;    Default: false
  ;   :prepare-f (function)(opt)}
  ;
  ; @example
  ;  (mongo-db/insert-document! "my_collection" {:namespace/id "MyObjectId" ...} {...})
  ;  =>
  ;  {:namespace/id "MyObjectId" ...}
  ;
  ; @return (namespaced map)
  ;  {:namespace/id (string)}
  ([collection-name document]
   (insert-document! collection-name document {}))

  ([collection-name document options]
   (if-let [document (as-> document % (checking/insert-input %)
                                      (preparing/insert-input collection-name % options)
                                      (adaptation/insert-input %))]
           (if-let [result (insert-and-return! collection-name document)]
                   (adaptation/insert-output result)))))



;; -- Inserting documents -----------------------------------------------------
;; ----------------------------------------------------------------------------

(defn insert-documents!
  ; @param (string) collection-name
  ; @param (namespaced maps in vector) documents
  ;  [{:namespace/id (string)(opt)}]
  ; @param (map)(opt) options
  ;  {:ordered? (boolean)(opt)
  ;    Default: false
  ;   :prepare-f (function)(opt)}
  ;
  ; @example
  ;  (mongo-db/insert-documents! "my_collection" [{:namespace/id "12ab3cd4efg5h6789ijk0420" ...}] {...})
  ;  =>
  ;  [{:namespace/id "12ab3cd4efg5h6789ijk0420" ...}]
  ;
  ; @return (namespaced maps in vector)
  ;  [{:namespace/id (string)}]
  ([collection-name documents]
   (insert-documents! collection-name documents {}))

  ([collection-name documents options]
   (vector/->items documents #(insert-document! collection-name % options))))



;; -- Saving document ---------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn save-document!
  ; @param (string) collection-name
  ; @param (namespaced map) document
  ;  {:namespace/id (string)(opt)}
  ; @param (map)(opt) options
  ;  {:ordered? (boolean)(opt)
  ;    Default: false
  ;   :prepare-f (function)(opt)}
  ;
  ; @example
  ;  (mongo-db/save-document! "my_collection" {:namespace/id "MyObjectId" ...} {...})
  ;  =>
  ;  {:namespace/id "MyObjectId" ...}
  ;
  ; @return (namespaced map)
  ;  {:namespace/id (string)}
  ([collection-name document]
   (save-document! collection-name document {}))

  ([collection-name document options]
   (if-let [document (as-> document % (checking/save-input %)
                                      (preparing/save-input collection-name % options)
                                      (adaptation/save-input %))]
           (if-let [result (save-and-return! collection-name document)]
                   (adaptation/save-output result)))))



;; -- Saving documents --------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn save-documents!
  ; @param (string) collection-name
  ; @param (namespaced maps in vector) documents
  ;  [{:namespace/id (string)(opt)}]
  ; @param (map)(opt) options
  ;  {:ordered? (boolean)(opt)
  ;    Default: false
  ;   :prepare-f (function)(opt)}
  ;
  ; @example
  ;  (mongo-db/save-documents! "my_collection" [{:namespace/id "MyObjectId" ...}] {...})
  ;  =>
  ;  [{:namespace/id "MyObjectId" ...}]
  ;
  ; @return (namespaced maps in vector)
  ;  [{:namespace/id (string)}]
  ([collection-name documents]
   (save-documents! collection-name documents {}))

  ([collection-name documents options]
   (vector/->items documents #(save-document! collection-name % options))))



;; -- Updating document -------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn update-document!
  ; @param (string) collection-name
  ; @param (map) query
  ;  {:namespace/id (string)(opt)}
  ; @param (map or namespaced map) document
  ; @param (map)(opt) options
  ;  {:prepare-f (function)(opt)}
  ;
  ; @usage
  ;  (mongo-db/update-document! "my_collection" {:namespace/score 100} {:namespace/score 0} {...})
  ;
  ; @usage
  ;  (mongo-db/update-document! "my_collection" {:$or [{...} {...}]} {:namespace/score 0} {...})
  ;
  ; @usage
  ;  (mongo-db/update-document! "my_collection" {:$or [{...} {...}]} {:$inc {:namespace/score 0}} {...})
  ;
  ; @return (boolean)
  ([collection-name query document]
   (update-document! collection-name query document {}))

  ([collection-name query document options]
   (boolean (if-let [document (as-> document % (checking/update-input %)
                                               (preparing/update-input collection-name % options)
                                               (adaptation/update-input %))]
                    (if-let [query (-> query checking/find-query adaptation/find-query)]
                            (let [result (update! collection-name query document {:multi false :upsert false})]
                                 (mrt/updated-existing? result)))))))



;; -- Updating documents ------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn update-documents!
  ; @param (string) collection-name
  ; @param (map) query
  ;  {:namespace/id (string)(opt)}
  ; @param (namespaced map) document
  ; @param (map)(opt) options
  ;  {:prepare-f (function)(opt)}
  ;
  ; @usage
  ;  (mongo-db/update-documents! "my_collection" {:namespace/score 100} {:namespace/score 0} {...})
  ;
  ; @usage
  ;  (mongo-db/update-documents! "my_collection" {:$or [{...} {...}]} {:namespace/score 0} {...})
  ;
  ; @usage
  ;  (mongo-db/update-documents! "my_collection" {:$or [{...} {...}]} {:$inc {:namespace/score 0}} {...})
  ;
  ; @return (boolean)
  ([collection-name query document]
   (update-documents! collection-name query document {}))

  ([collection-name query document options]
   (boolean (if-let [document (as-> document % (checking/update-input %)
                                               (preparing/update-input collection-name % options)
                                               (adaptation/update-input %))]
                    (if-let [query (-> query checking/find-query adaptation/find-query)]
                            ; WARNING! DO NOT USE!
                            ; java.lang.IllegalArgumentException: Replacements can not be multi
                            (let [result (update! collection-name query document {:multi true :upsert false})]
                                 (mrt/updated-existing? result)))))))



;; -- Upserting document ------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn upsert-document!
  ; @param (string) collection-name
  ; @param (map) query
  ; @param (map or namespaced map) document
  ; @param (map)(opt) options
  ;  {:ordered? (boolean)(opt)
  ;    Default: false
  ;   :prepare-f (function)(opt)}
  ;
  ; @usage
  ;  (mongo-db/upsert-document! "my_collection" {:namespace/score 100} {:namespace/score 0} {...})
  ;
  ; @usage
  ;  (mongo-db/upsert-document! "my_collection" {:$or [{...} {...}]} {:namespace/score 0} {...})
  ;
  ; @usage
  ;  (mongo-db/upsert-document! "my_collection" {:$or [{...} {...}]} {:$inc {:namespace/score 0}} {...})
  ;
  ; @return (boolean)
  ([collection-name query document]
   (upsert-document! collection-name query document {}))

  ([collection-name query document options]
   (boolean (if-let [document (as-> document % (checking/upsert-input %)
                                               (preparing/upsert-input collection-name % options)
                                               (adaptation/upsert-input %))]
                    (if-let [query (-> query checking/find-query adaptation/find-query)]
                            (let [result (upsert! collection-name query document {:multi false})]
                                 (mrt/acknowledged? result)))))))



;; -- Upserting documents -----------------------------------------------------
;; ----------------------------------------------------------------------------

(defn upsert-documents!
  ; @param (string) collection-name
  ; @param (map) query
  ; @param (namespaced map) document
  ; @param (map)(opt) options
  ;  {:ordered? (boolean)(opt)
  ;   :prepare-f (function)(opt)}
  ;
  ; @usage
  ;  (mongo-db/upsert-documents! "my_collection" {:namespace/score 100} {:namespace/score 0} {...})
  ;
  ; @usage
  ;  (mongo-db/upsert-documents! "my_collection" {:$or [{...} {...}]} {:namespace/score 0} {...})
  ;
  ; @usage
  ;  (mongo-db/upsert-documents! "my_collection" {:$or [{...} {...}]} {:$inc {:namespace/score 0}} {...})
  ;
  ; @return (boolean)
  ([collection-name query document]
   (upsert-documents! collection-name query document {}))

  ([collection-name query document options]
   (boolean (if-let [document (as-> document % (checking/upsert-input %)
                                               (preparing/upsert-input collection-name % options)
                                               (adaptation/upsert-input %))]
                    (if-let [query (-> query checking/find-query adaptation/find-query)]
                            ; WARNING! DO NOT USE!
                            ; java.lang.IllegalArgumentException: Replacements can not be multi
                            (let [result (upsert! collection-name query document {:multi true})]
                                 (mrt/acknowledged? result)))))))



;; -- Applying document -------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn apply-document!
  ; @param (string) collection-name
  ; @param (string) document-id
  ; @param (function) f
  ; @param (map)(opt) options
  ;  {:postpare-f (function)(opt)
  ;   :prepare-f (function)(opt)}
  ;
  ; @usage
  ;  (mongo-db/apply-document! "my_collection" "MyObjectId" #(assoc % :namespace/color "Blue") {...})
  ;
  ; @return (namespaced map)
  ([collection-name document-id f]
   (apply-document! collection-name document-id f {}))

  ([collection-name document-id f options]
   ; A prepare-f az f fügvvény alkalmazása előtt, a postpare-f függvény pedig az
   ; f függvény alkalmazása után van használva.
   (if-let [document (reader/get-document-by-id collection-name document-id)]
           (if-let [document (preparing/apply-input collection-name document options)]
                   (if-let [document (f document)]
                           (if-let [document (postparing/apply-input collection-name document options)]
                                   (if-let [document (adaptation/save-input document)]
                                           (let [result (save-and-return! collection-name document)]
                                                (adaptation/save-output result)))))))))



;; -- Applying documents ------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn apply-documents!
  ; @param (string) collection-name
  ; @param (function) f
  ; @param (map)(opt) options
  ;  {:postpare-f (function)(opt)
  ;   :prepare-f (function)(opt)}
  ;
  ; @usage
  ;  (mongo-db/apply-document! "my_collection" #(assoc % :namespace/color "Blue") {...})
  ;
  ; @return (namespaced maps in vector)
  ([collection-name f]
   (apply-documents! collection-name f {}))

  ([collection-name f options]
   ; XXX#9801
   (if-let [collection (reader/get-collection collection-name)]
           (letfn [(fi [result document]
                       (if-let [document (f document)]
                               (let [document (save-document! collection-name document options)]
                                    (conj result document))
                               (return result)))]
                  (reduce fi [] collection)))))



;; -- Removing document -------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn- remove-unordered-document!
  ; WARNING! NON-PUBLIC! DO NOT USE!
  ;
  ; @param (string) collection-name
  ; @param (string) document-id
  ; @param (map) options
  ;
  ; @return (string)
  [collection-name document-id _]
  (if-let [document-id (adaptation/document-id-input document-id)]
          (let [result (remove-by-id! collection-name document-id)]
               (if (mrt/acknowledged? result)
                   (adaptation/document-id-output document-id)))))

(defn- remove-ordered-document!
  ; WARNING! NON-PUBLIC! DO NOT USE!
  ;
  ; @param (string) collection-name
  ; @param (string) document-id
  ; @param (map) options
  ;
  ; @return (string)
  [collection-name document-id _]
  (if-let [document-id (adaptation/document-id-input document-id)]
          (do (let [document-id (adaptation/document-id-output document-id)]
                   (reorder-following-documents! collection-name document-id {:operation :decrease}))
              (let [result (remove-by-id! collection-name document-id)]
                   (if (mrt/acknowledged? result)
                       (adaptation/document-id-output document-id))))))

(defn remove-document!
  ; @param (string) collection-name
  ; @param (string) document-id
  ; @param (map)(opt) options
  ;  {:ordered? (boolean)
  ;    Default: false}
  ;
  ; @example
  ;  (mongo-db/remove-document "my_collection" "MyObjectId" {...})
  ;  =>
  ;  "MyObjectId"
  ;
  ; @return (string)
  ([collection-name document-id]
   (remove-document! collection-name document-id {}))

  ([collection-name document-id {:keys [ordered?] :as options}]
   (if ordered? (remove-ordered-document!   collection-name document-id options)
                (remove-unordered-document! collection-name document-id options))))



;; -- Removing documents ------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn remove-documents!
  ; @param (string) collection-name
  ; @param (strings in vector) document-ids
  ; @param (map)(opt) options
  ;  {:ordered? (boolean)
  ;    Default: false}
  ;
  ; @example
  ;  (mongo-db/remove-documents! "my_collection" ["MyObjectId" "YourObjectId"] {...})
  ;  =>
  ;  ["MyObjectId" "YourObjectId"]
  ;
  ; @return (strings in vector)
  ([collection-name document-ids]
   (remove-documents! collection-name document-ids {}))

  ([collection-name document-ids options]
   (vector/->items document-ids #(remove-document! collection-name % options))))



;; -- Removing documents ------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn remove-all-documents!
  ; @param (string) collection-name
  ;
  ; @usage
  ;  (mongo-db/remove-all-documents! "my_collection")
  ;
  ; @return (?)
  [collection-name]
  (drop! collection-name))



;; -- Duplicating document ----------------------------------------------------
;; ----------------------------------------------------------------------------

(defn- duplicate-unordered-document!
  ; WARNING! NON-PUBLIC! DO NOT USE!
  ;
  ; @param (string) collection-name
  ; @param (string) document-id
  ; @param (map) options
  ;
  ; @return (namespaced map)
  [collection-name document-id options]
  (if-let [document (reader/get-document-by-id collection-name document-id)]
          (if-let [document-copy (preparing/duplicate-input collection-name document options)]
                  (if-let [document-copy (adaptation/duplicate-input document-copy)]
                          (let [result (insert-and-return! collection-name document-copy)]
                               (adaptation/duplicate-output result))))))

(defn- duplicate-ordered-document!
  ; WARNING! NON-PUBLIC! DO NOT USE!
  ;
  ; @param (string) collection-name
  ; @param (string) document-id
  ; @param (map) options
  ;
  ; @return (namespaced map)
  [collection-name document-id options]
  (if-let [document (reader/get-document-by-id collection-name document-id)]
          (if-let [document-copy (preparing/duplicate-input collection-name document options)]
                  (if-let [document-copy (adaptation/duplicate-input document-copy)]
                          (do (reorder-following-documents! collection-name document-id {:operation :increase})
                              (let [result (insert-and-return! collection-name document-copy)]
                                   (adaptation/duplicate-output result)))))))

(defn duplicate-document!
  ; @param (string) collection-name
  ; @param (string) document-id
  ; @param (map)(opt) options
  ;  {:changes (namespaced map)(opt)
  ;   :label-key (namespaced keyword)(opt)
  ;    A dokumentum melyik kulcsának értékéhez fűzze hozzá a "#..." kifejezést
  ;   :ordered? (boolean)(opt)
  ;    Default: false
  ;   :prepare-f (function)(opt)}
  ;
  ; @example
  ;  (mongo-db/duplicate-document! "my_collection" "MyObjectId" {...})
  ;  =>
  ;  {:namespace/id "MyObjectId" :namespace/label "My document"}
  ;
  ; @example
  ;  (mongo-db/duplicate-document! "my_collection" "MyObjectId" {:label-key :namespace/label})
  ;  =>
  ;  {:namespace/id "MyObjectId" :namespace/label "My document #2"}
  ;
  ; @return (namespaced map)
  ;  {:namespace/id (string)}
  ([collection-name document-id]
   (duplicate-document! collection-name document-id {}))

  ([collection-name document-id {:keys [ordered?] :as options}]
   (if ordered? (duplicate-ordered-document!   collection-name document-id options)
                (duplicate-unordered-document! collection-name document-id options))))



;; -- Duplicating documents ---------------------------------------------------
;; ----------------------------------------------------------------------------

(defn duplicate-documents!
  ; @param (string) collection-name
  ; @param (strings in vector) document-ids
  ; @param (map)(opt) options
  ;  {:label-key (namespaced keyword)(opt)
  ;    A dokumentum melyik kulcsának értékéhez fűzze hozzá a "#..." kifejezést
  ;   :ordered? (boolean)(opt)
  ;    Default: false
  ;   :prepare-f (function)(opt)}
  ;
  ; @example
  ;  (mongo-db/duplicate-documents! "my_collection" ["MyObjectId" "YourObjectId"] {...})
  ;  =>
  ;  [{...} {...}]
  ;
  ; @return (namespaced maps in vector)
  ([collection-name document-ids]
   (duplicate-documents! collection-name document-ids {}))

  ([collection-name document-ids options]
   (vector/->items document-ids #(duplicate-document! collection-name % options))))



;; -- Reordering collection ---------------------------------------------------
;; ----------------------------------------------------------------------------

(defn reorder-documents!
  ; @param (string) collection-name
  ; @param (vectors in vector) document-order
  ;  [[(string) document-id
  ;    (integer) document-dex]]
  ;
  ; @usage
  ;  (mongo-db/reorder-documents "my_collection" [["MyObjectId" 1] ["YourObjectId" 2]])
  ;
  ; @return (vectors in vector)
  [collection-name document-order]
  (let [namespace (reader/get-collection-namespace collection-name)
        order-key (keyword/add-namespace namespace :order)]
       (letfn [(f [[document-id document-dex]]
                  (if-let [document-id (adaptation/document-id-input document-id)]
                          (let [result (update! collection-name {:_id document-id}
                                                                {"$set" {order-key document-dex}})]
                               (if (mrt/acknowledged? result)
                                   (return [document-id document-dex])))))]
              (vector/->items document-order f))))

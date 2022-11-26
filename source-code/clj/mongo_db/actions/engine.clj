
(ns mongo-db.actions.engine
    (:require [candy.api                   :refer [return]]
              [keyword.api                 :as keyword]
              [map.api                     :as map]
              [monger.result               :as mrt]
              [mongo-db.actions.checking   :as actions.checking]
              [mongo-db.actions.helpers    :as actions.helpers]
              [mongo-db.actions.adaptation :as actions.adaptation]
              [mongo-db.actions.postparing :as actions.postparing]
              [mongo-db.actions.preparing  :as actions.preparing]
              [mongo-db.reader.checking    :as reader.checking]
              [mongo-db.reader.adaptation  :as reader.adaptation]
              [mongo-db.core.errors        :as core.errors]
              [mongo-db.reader.engine      :as reader.engine]
              [vector.api                  :as vector]))

;; -- Reordering following documents ------------------------------------------
;; ----------------------------------------------------------------------------

(defn- reorder-following-documents!
  ; @param (string) collection-name
  ; @param (string) document-id
  ; @param (map) options
  ; {:operation (keyword)
  ;   :decrease, :increase}
  ;
  ; @return (namespaced map)
  [collection-name document-id {:keys [operation]}]
  ; Egy rendezett kollekcióból történő dokumentum eltávolítása a dokumentum után sorrendben
  ; következő többi dokumentum pozíciójának csökkentését teszi szükségessé.
  ;
  ; Egy rendezett kollekcióba történő dokumentum beszúrása a dokumentum után sorrendben következő
  ; többi dokumentum pozíciójának növelését teszi szükségessé.
  (if-let [document (reader.engine/get-document-by-id collection-name document-id)]
          (let [namespace    (map/get-namespace     document)
                order-key    (keyword/add-namespace namespace :order)
                document-dex (get document order-key)
                query        {order-key {:$gt document-dex}}
                document     (case operation :increase {:$inc {order-key  1}}
                                             :decrease {:$inc {order-key -1}})]
               (if-let [query (-> query actions.checking/update-query actions.adaptation/update-query)]
                       (if-let [document (-> document actions.checking/update-input actions.adaptation/update-input)]
                               ; A sorrendben a dokumentum után következő dokumentumok sorrendbeli pozíciójának eltolása
                               (let [result (actions.helpers/update! collection-name query document {:multi true})]
                                    (if-not (mrt/acknowledged? result)
                                            (throw (Exception. core.errors/REORDER-DOCUMENTS-FAILED)))))))
          (throw (Exception. core.errors/DOCUMENT-DOES-NOT-EXISTS-ERROR))))

;; -- Inserting document ------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn insert-document!
  ; @param (string) collection-name
  ; @param (namespaced map) document
  ; {:namespace/id (string)(opt)}
  ; @param (map)(opt) options
  ; {:ordered? (boolean)(opt)
  ;   Default: false
  ;  :prepare-f (function)(opt)}
  ;
  ; @example
  ; (insert-document! "my_collection" {:namespace/id "MyObjectId" ...} {...})
  ; =>
  ; {:namespace/id "MyObjectId" ...}
  ;
  ; @return (namespaced map)
  ; {:namespace/id (string)}
  ([collection-name document]
   (insert-document! collection-name document {}))

  ([collection-name document options]
   (if-let [document (as-> document % (actions.checking/insert-input %)
                                      (actions.preparing/insert-input collection-name % options)
                                      (actions.adaptation/insert-input %))]
           (if-let [result (actions.helpers/insert-and-return! collection-name document)]
                   (actions.adaptation/insert-output result)))))

;; -- Inserting documents -----------------------------------------------------
;; ----------------------------------------------------------------------------

(defn insert-documents!
  ; @param (string) collection-name
  ; @param (namespaced maps in vector) documents
  ; [{:namespace/id (string)(opt)}]
  ; @param (map)(opt) options
  ; {:ordered? (boolean)(opt)
  ;   Default: false
  ;  :prepare-f (function)(opt)}
  ;
  ; @example
  ; (insert-documents! "my_collection" [{:namespace/id "12ab3cd4efg5h6789ijk0420" ...}] {...})
  ; =>
  ; [{:namespace/id "12ab3cd4efg5h6789ijk0420" ...}]
  ;
  ; @return (namespaced maps in vector)
  ; [{:namespace/id (string)}]
  ([collection-name documents]
   (insert-documents! collection-name documents {}))

  ([collection-name documents options]
   (vector/->items documents #(insert-document! collection-name % options))))

;; -- Saving document ---------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn save-document!
  ; @param (string) collection-name
  ; @param (namespaced map) document
  ; {:namespace/id (string)(opt)}
  ; @param (map)(opt) options
  ; {:ordered? (boolean)(opt)
  ;   Default: false
  ;  :prepare-f (function)(opt)}
  ;
  ; @example
  ; (save-document! "my_collection" {:namespace/id "MyObjectId" ...} {...})
  ; =>
  ; {:namespace/id "MyObjectId" ...}
  ;
  ; @return (namespaced map)
  ; {:namespace/id (string)}
  ([collection-name document]
   (save-document! collection-name document {}))

  ([collection-name document options]
   (if-let [document (as-> document % (actions.checking/save-input %)
                                      (actions.preparing/save-input collection-name % options)
                                      (actions.adaptation/save-input %))]
           (if-let [result (actions.helpers/save-and-return! collection-name document)]
                   (actions.adaptation/save-output result)))))

;; -- Saving documents --------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn save-documents!
  ; @param (string) collection-name
  ; @param (namespaced maps in vector) documents
  ; [{:namespace/id (string)(opt)}]
  ; @param (map)(opt) options
  ; {:ordered? (boolean)(opt)
  ;   Default: false
  ;  :prepare-f (function)(opt)}
  ;
  ; @example
  ; (save-documents! "my_collection" [{:namespace/id "MyObjectId" ...}] {...})
  ; =>
  ; [{:namespace/id "MyObjectId" ...}]
  ;
  ; @return (namespaced maps in vector)
  ; [{:namespace/id (string)}]
  ([collection-name documents]
   (save-documents! collection-name documents {}))

  ([collection-name documents options]
   (vector/->items documents #(save-document! collection-name % options))))

;; -- Updating document -------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn update-document!
  ; @param (string) collection-name
  ; @param (map) query
  ; {:namespace/id (string)(opt)}
  ; @param (map or namespaced map) document
  ; @param (map)(opt) options
  ; {:prepare-f (function)(opt)}
  ;
  ; @usage
  ; (update-document! "my_collection" {:namespace/score 100} {:namespace/score 0} {...})
  ;
  ; @usage
  ; (update-document! "my_collection" {:$or [{...} {...}]} {:namespace/score 0} {...})
  ;
  ; @usage
  ; (update-document! "my_collection" {:$or [{...} {...}]} {:$inc {:namespace/score 0}} {...})
  ;
  ; @return (boolean)
  ([collection-name query document]
   (update-document! collection-name query document {}))

  ([collection-name query document options]
   (boolean (if-let [document (as-> document % (actions.checking/update-input %)
                                               (actions.preparing/update-input collection-name % options)
                                               (actions.adaptation/update-input %))]
                    (if-let [query (-> query reader.checking/find-query reader.adaptation/find-query)]
                            (let [result (actions.helpers/update! collection-name query document {:multi false :upsert false})]
                                 (mrt/updated-existing? result)))))))

;; -- Updating documents ------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn update-documents!
  ; @param (string) collection-name
  ; @param (map) query
  ; {:namespace/id (string)(opt)}
  ; @param (namespaced map) document
  ; @param (map)(opt) options
  ; {:prepare-f (function)(opt)}
  ;
  ; @usage
  ; (update-documents! "my_collection" {:namespace/score 100} {:namespace/score 0} {...})
  ;
  ; @usage
  ; (update-documents! "my_collection" {:$or [{...} {...}]} {:namespace/score 0} {...})
  ;
  ; @usage
  ; (update-documents! "my_collection" {:$or [{...} {...}]} {:$inc {:namespace/score 0}} {...})
  ;
  ; @return (boolean)
  ([collection-name query document]
   (update-documents! collection-name query document {}))

  ([collection-name query document options]
   (boolean (if-let [document (as-> document % (actions.checking/update-input %)
                                               (actions.preparing/update-input collection-name % options)
                                               (actions.adaptation/update-input %))]
                    (if-let [query (-> query reader.checking/find-query reader.adaptation/find-query)]
                            ; WARNING! DO NOT USE!
                            ; java.lang.IllegalArgumentException: Replacements can not be multi
                            (let [result (actions.helpers/update! collection-name query document {:multi true :upsert false})]
                                 (mrt/updated-existing? result)))))))

;; -- Upserting document ------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn upsert-document!
  ; @param (string) collection-name
  ; @param (map) query
  ; @param (map or namespaced map) document
  ; @param (map)(opt) options
  ; {:ordered? (boolean)(opt)
  ;   Default: false
  ;  :prepare-f (function)(opt)}
  ;
  ; @usage
  ; (upsert-document! "my_collection" {:namespace/score 100} {:namespace/score 0} {...})
  ;
  ; @usage
  ; (upsert-document! "my_collection" {:$or [{...} {...}]} {:namespace/score 0} {...})
  ;
  ; @usage
  ; (upsert-document! "my_collection" {:$or [{...} {...}]} {:$inc {:namespace/score 0}} {...})
  ;
  ; @return (boolean)
  ([collection-name query document]
   (upsert-document! collection-name query document {}))

  ([collection-name query document options]
   (boolean (if-let [document (as-> document % (actions.checking/upsert-input %)
                                               (actions.preparing/upsert-input collection-name % options)
                                               (actions.adaptation/upsert-input %))]
                    (if-let [query (-> query reader.checking/find-query reader.adaptation/find-query)]
                            (let [result (actions.helpers/upsert! collection-name query document {:multi false})]
                                 (mrt/acknowledged? result)))))))

;; -- Upserting documents -----------------------------------------------------
;; ----------------------------------------------------------------------------

(defn upsert-documents!
  ; @param (string) collection-name
  ; @param (map) query
  ; @param (namespaced map) document
  ; @param (map)(opt) options
  ; {:ordered? (boolean)(opt)
  ;  :prepare-f (function)(opt)}
  ;
  ; @usage
  ; (upsert-documents! "my_collection" {:namespace/score 100} {:namespace/score 0} {...})
  ;
  ; @usage
  ; (upsert-documents! "my_collection" {:$or [{...} {...}]} {:namespace/score 0} {...})
  ;
  ; @usage
  ; (upsert-documents! "my_collection" {:$or [{...} {...}]} {:$inc {:namespace/score 0}} {...})
  ;
  ; @return (boolean)
  ([collection-name query document]
   (upsert-documents! collection-name query document {}))

  ([collection-name query document options]
   (boolean (if-let [document (as-> document % (actions.checking/upsert-input %)
                                               (actions.preparing/upsert-input collection-name % options)
                                               (actions.adaptation/upsert-input %))]
                    (if-let [query (-> query reader.checking/find-query reader.adaptation/find-query)]
                            ; WARNING! DO NOT USE!
                            ; java.lang.IllegalArgumentException: Replacements can not be multi
                            (let [result (actions.helpers/upsert! collection-name query document {:multi true})]
                                 (mrt/acknowledged? result)))))))

;; -- Applying document -------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn apply-document!
  ; @param (string) collection-name
  ; @param (string) document-id
  ; @param (function) f
  ; @param (map)(opt) options
  ; {:postpare-f (function)(opt)
  ;  :prepare-f (function)(opt)}
  ;
  ; @usage
  ; (apply-document! "my_collection" "MyObjectId" #(assoc % :namespace/color "Blue") {...})
  ;
  ; @return (namespaced map)
  ([collection-name document-id f]
   (apply-document! collection-name document-id f {}))

  ([collection-name document-id f options]
   ; A prepare-f az f fügvvény alkalmazása előtt, a postpare-f függvény pedig az
   ; f függvény alkalmazása után van használva.
   (if-let [document (reader.engine/get-document-by-id collection-name document-id)]
           (if-let [document (actions.preparing/apply-input collection-name document options)]
                   (if-let [document (f document)]
                           (if-let [document (actions.postparing/apply-input collection-name document options)]
                                   (if-let [document (actions.adaptation/save-input document)]
                                           (let [result (actions.helpers/save-and-return! collection-name document)]
                                                (actions.adaptation/save-output result)))))))))

;; -- Applying documents ------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn apply-documents!
  ; @param (string) collection-name
  ; @param (function) f
  ; @param (map)(opt) options
  ; {:postpare-f (function)(opt)
  ;  :prepare-f (function)(opt)}
  ;
  ; @usage
  ; (apply-document! "my_collection" #(assoc % :namespace/color "Blue") {...})
  ;
  ; @return (namespaced maps in vector)
  ([collection-name f]
   (apply-documents! collection-name f {}))

  ([collection-name f options]
   ; XXX#9801
   (if-let [collection (reader.engine/get-collection collection-name)]
           (letfn [(fi [result document]
                       (if-let [document (f document)]
                               (let [document (save-document! collection-name document options)]
                                    (conj result document))
                               (return result)))]
                  (reduce fi [] collection)))))

;; -- Removing document -------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn- remove-unordered-document!
  ; @param (string) collection-name
  ; @param (string) document-id
  ; @param (map) options
  ;
  ; @return (string)
  [collection-name document-id _]
  (if-let [document-id (actions.adaptation/document-id-input document-id)]
          (let [result (actions.helpers/remove-by-id! collection-name document-id)]
               (if (mrt/acknowledged? result)
                   (actions.adaptation/document-id-output document-id)))))

(defn- remove-ordered-document!
  ; @param (string) collection-name
  ; @param (string) document-id
  ; @param (map) options
  ;
  ; @return (string)
  [collection-name document-id _]
  (if-let [document-id (actions.adaptation/document-id-input document-id)]
          (do (let [document-id (actions.adaptation/document-id-output document-id)]
                   (reorder-following-documents! collection-name document-id {:operation :decrease}))
              (let [result (actions.helpers/remove-by-id! collection-name document-id)]
                   (if (mrt/acknowledged? result)
                       (actions.adaptation/document-id-output document-id))))))

(defn remove-document!
  ; @param (string) collection-name
  ; @param (string) document-id
  ; @param (map)(opt) options
  ; {:ordered? (boolean)
  ;   Default: false}
  ;
  ; @example
  ; (remove-document "my_collection" "MyObjectId" {...})
  ; =>
  ; "MyObjectId"
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
  ; {:ordered? (boolean)
  ;   Default: false}
  ;
  ; @example
  ; (remove-documents! "my_collection" ["MyObjectId" "YourObjectId"] {...})
  ; =>
  ; ["MyObjectId" "YourObjectId"]
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
  ; (remove-all-documents! "my_collection")
  ;
  ; @return (?)
  [collection-name]
  (actions.helpers/drop! collection-name))

;; -- Duplicating document ----------------------------------------------------
;; ----------------------------------------------------------------------------

(defn- duplicate-unordered-document!
  ; @param (string) collection-name
  ; @param (string) document-id
  ; @param (map) options
  ;
  ; @return (namespaced map)
  [collection-name document-id options]
  (if-let [document (reader.engine/get-document-by-id collection-name document-id)]
          (if-let [document-copy (actions.preparing/duplicate-input collection-name document options)]
                  (if-let [document-copy (actions.adaptation/duplicate-input document-copy)]
                          (let [result (actions.helpers/insert-and-return! collection-name document-copy)]
                               (actions.adaptation/duplicate-output result))))))

(defn- duplicate-ordered-document!
  ; @param (string) collection-name
  ; @param (string) document-id
  ; @param (map) options
  ;
  ; @return (namespaced map)
  [collection-name document-id options]
  (if-let [document (reader.engine/get-document-by-id collection-name document-id)]
          (if-let [document-copy (actions.preparing/duplicate-input collection-name document options)]
                  (if-let [document-copy (actions.adaptation/duplicate-input document-copy)]
                          (do (reorder-following-documents! collection-name document-id {:operation :increase})
                              (let [result (actions.helpers/insert-and-return! collection-name document-copy)]
                                   (actions.adaptation/duplicate-output result)))))))

(defn duplicate-document!
  ; @param (string) collection-name
  ; @param (string) document-id
  ; @param (map)(opt) options
  ; {:changes (namespaced map)(opt)
  ;  :label-key (namespaced keyword)(opt)
  ;   A dokumentum melyik kulcsának értékéhez fűzze hozzá a "#..." kifejezést
  ;  :ordered? (boolean)(opt)
  ;   Default: false
  ;  :prepare-f (function)(opt)}
  ;
  ; @example
  ; (duplicate-document! "my_collection" "MyObjectId" {...})
  ; =>
  ; {:namespace/id "MyObjectId" :namespace/label "My document"}
  ;
  ; @example
  ; (duplicate-document! "my_collection" "MyObjectId" {:label-key :namespace/label})
  ; =>
  ; {:namespace/id "MyObjectId" :namespace/label "My document #2"}
  ;
  ; @return (namespaced map)
  ; {:namespace/id (string)}
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
  ; {:label-key (namespaced keyword)(opt)
  ;   A dokumentum melyik kulcsának értékéhez fűzze hozzá a "#..." kifejezést
  ;  :ordered? (boolean)(opt)
  ;   Default: false
  ;  :prepare-f (function)(opt)}
  ;
  ; @example
  ; (duplicate-documents! "my_collection" ["MyObjectId" "YourObjectId"] {...})
  ; =>
  ; [{...} {...}]
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
  ; [[(string) document-id
  ;   (integer) document-dex]]
  ;
  ; @usage
  ; (reorder-documents "my_collection" [["MyObjectId" 1] ["YourObjectId" 2]])
  ;
  ; @return (vectors in vector)
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

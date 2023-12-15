
(ns mongo-db.actions.engine
    (:require [fruits.keyword.api            :as keyword]
              [fruits.map.api                :as map]
              [fruits.vector.api             :as vector]
              [monger.result                 :as mrt]
              [mongo-db.actions.adaptation   :as actions.adaptation]
              [mongo-db.actions.checking     :as actions.checking]
              [mongo-db.actions.postparing   :as actions.postparing]
              [mongo-db.actions.preparing    :as actions.preparing]
              [mongo-db.actions.prototyping  :as actions.prototyping]
              [mongo-db.actions.side-effects :as actions.side-effects]
              [mongo-db.core.messages          :as core.messages]
              [mongo-db.reader.adaptation    :as reader.adaptation]
              [mongo-db.reader.checking      :as reader.checking]
              [mongo-db.reader.engine        :as reader.engine]))

;; -- Reordering following documents ------------------------------------------
;; ----------------------------------------------------------------------------

(defn- reorder-following-documents!
  ; @ignore
  ;
  ; @description
  ; - Adjusts the ':order' value of documents in a collection by either increasing
  ;   or decreasing the positions of documents following a specified one (in terms
  ;   of updating their ':order' value). It is used to maintain the sequence
  ;   of documents in an ordered collection when items are added or removed.
  ;
  ; @param (string) collection-path
  ; @param (string) document-id
  ; @param (map) options
  ; {:operation (keyword)
  ;   :decrease, :increase}
  ;
  ; @return (namespaced map)
  [collection-path document-id {:keys [operation]}]
  ; In an ordered collection ...
  ; ... removing a document requires to update (decrease) the position (':order' value)
  ;     of other documents that are follow (in terms of ':order' value) the just
  ;     removed document.
  ; ... inserting a document requires to update (increase) the position (':order' value)
  ;     of other documents that are follow (in terms of ':order' value) the just
  ;     inserted document.
  (if-let [document (reader.engine/get-document-by-id collection-path document-id)]
          (let [namespace    (map/namespace document)
                order-key    (keyword/add-namespace :order namespace)
                document-dex (get document order-key)
                query        {order-key {:$gt document-dex}}
                document     (case operation :increase {:$inc {order-key  1}}
                                             :decrease {:$inc {order-key -1}})]
               (if-let [query (-> query actions.checking/update-query actions.adaptation/update-query)]
                       (if-let [document (-> document actions.checking/update-input actions.adaptation/update-input)]
                               ; Adjusting the ':order' value of other documents that are follow (in terms of ':order' value)
                               ; the just inserted or removed document.
                               (let [result (actions.side-effects/update! collection-path query document {:multi true})]
                                    (if-not (mrt/acknowledged? result)
                                            (throw (Exception. core.messages/REORDERING-DOCUMENTS-FAILED)))))))
          (throw (Exception. core.messages/DOCUMENT-DOES-NOT-EXISTS-ERROR))))

;; -- Inserting document ------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn insert-document!
  ; @description
  ; - Inserts a document into a collection.
  ; - You can apply custom functions for preparing and prototyping the document.
  ; - Returns the inserted document.
  ;
  ; @param (string) collection-path
  ; @param (namespaced map) document
  ; No need to be a namespaced map if using a prototype function that converts it
  ; into a namespaced form!
  ; {:namespace/id (string)(opt)}
  ; @param (map)(opt) options
  ; {:ordered? (boolean)(opt)
  ;   Set to TRUE when inserting a document into an ordered collection!
  ;   Default: false
  ;  :prepare-f (function)(opt)
  ;   Applied on the input document.
  ;  :prototype-f (function)(opt)
  ;   Applied on the input document before any checking or preparing.
  ;   Must return a namespaced map!}
  ;
  ; @usage
  ; (insert-document! "my_collection" {:namespace/id "MyObjectId" ...} {...})
  ;
  ; @example
  ; (insert-document! "my_collection" {:namespace/id "MyObjectId" ...} {...})
  ; =>
  ; {:namespace/id "MyObjectId" ...}
  ;
  ; @return (namespaced map)
  ; {:namespace/id (string)}
  ([collection-path document]
   (insert-document! collection-path document {}))

  ([collection-path document options]
   ; XXX#7100
   ; The checking function must be applied before the preparing function because
   ; document preparing requires documents as namespaced maps and the checking
   ; function checks whether a document is a namespaced map!
   (if-let [document (as-> document % (actions.prototyping/insert-input collection-path % options)
                                      (actions.checking/insert-input %)
                                      (actions.preparing/insert-input collection-path % options)
                                      (actions.adaptation/insert-input %))]
           (if-let [result (actions.side-effects/insert-and-return! collection-path document)]
                   (actions.adaptation/insert-output result)))))

;; -- Inserting documents -----------------------------------------------------
;; ----------------------------------------------------------------------------

(defn insert-documents!
  ; @description
  ; - Inserts multiple documents into a collection.
  ; - You can apply custom functions for preparing and prototyping each document.
  ; - Returns the inserted documents in a vector.
  ;
  ; @param (string) collection-path
  ; @param (namespaced maps in vector) documents
  ; No need to be a namespaced map if using a prototype function that converts it
  ; into a namespaced form!
  ; [{:namespace/id (string)(opt)}]
  ; @param (map)(opt) options
  ; {:ordered? (boolean)(opt)
  ;   Set to TRUE when inserting documents into an ordered collection!
  ;   Default: false
  ;  :prepare-f (function)(opt)
  ;   Applied on each input document.
  ;  :prototype-f (function)(opt)
  ;   Applied on each input document before any checking or preparing.
  ;   Must return a namespaced map!}
  ;
  ; @usage
  ; (insert-documents! "my_collection" [{:namespace/id "12ab3cd4efg5h6789ijk0420" ...}] {...})
  ;
  ; @example
  ; (insert-documents! "my_collection" [{:namespace/id "12ab3cd4efg5h6789ijk0420" ...}] {...})
  ; =>
  ; [{:namespace/id "12ab3cd4efg5h6789ijk0420" ...}]
  ;
  ; @return (namespaced maps in vector)
  ; [{:namespace/id (string)}]
  ([collection-path documents]
   (insert-documents! collection-path documents {}))

  ([collection-path documents options]
   (vector/->items documents #(insert-document! collection-path % options))))

;; -- Saving document ---------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn save-document!
  ; @description
  ; - Saves a document to a collection.
  ; - You can apply custom functions for preparing and prototyping the document.
  ; - Returns the saved document.
  ;
  ; @param (string) collection-path
  ; @param (namespaced map) document
  ; No need to be a namespaced map if using a prototype function that converts it
  ; into a namespaced form!
  ; {:namespace/id (string)(opt)}
  ; @param (map)(opt) options
  ; {:ordered? (boolean)(opt)
  ;   Set to TRUE when saving a document to an ordered collection!
  ;   Default: false
  ;  :prepare-f (function)(opt)
  ;   Applied on the input document.
  ;  :prototype-f (function)(opt)
  ;   Applied on the input document before any checking or preparing.
  ;   Must return a namespaced map!}
  ;
  ; @usage
  ; (save-document! "my_collection" {:namespace/id "MyObjectId" ...} {...})
  ;
  ; @example
  ; (save-document! "my_collection" {:namespace/id "MyObjectId" ...} {...})
  ; =>
  ; {:namespace/id "MyObjectId" ...}
  ;
  ; @return (namespaced map)
  ; {:namespace/id (string)}
  ([collection-path document]
   (save-document! collection-path document {}))

  ([collection-path document options]
   ; XXX#7100
   (if-let [document (as-> document % (actions.prototyping/save-input collection-path % options)
                                      (actions.checking/save-input %)
                                      (actions.preparing/save-input collection-path % options)
                                      (actions.adaptation/save-input %))]
           (if-let [result (actions.side-effects/save-and-return! collection-path document)]
                   (actions.adaptation/save-output result)))))

;; -- Saving documents --------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn save-documents!
  ; @description
  ; - Saves multiple documents to a collection.
  ; - You can apply custom functions for preparing and prototyping each document.
  ; - Returns the saved documents in a vector.
  ;
  ; @param (string) collection-path
  ; @param (namespaced maps in vector) documents
  ; No need to be a namespaced map if using a prototype function that converts it
  ; into a namespaced form!
  ; [{:namespace/id (string)(opt)}]
  ; @param (map)(opt) options
  ; {:ordered? (boolean)(opt)
  ;   Set to TRUE when saving documents to an ordered collection!
  ;   Default: false
  ;  :prepare-f (function)(opt)
  ;   Applied on each input document.
  ;  :prototype-f (function)(opt)
  ;   Applied on each input document before any checking or preparing.
  ;   Must return a namespaced map!}
  ;
  ; @usage
  ; (save-documents! "my_collection" [{:namespace/id "MyObjectId" ...}] {...})
  ;
  ; @example
  ; (save-documents! "my_collection" [{:namespace/id "MyObjectId" ...}] {...})
  ; =>
  ; [{:namespace/id "MyObjectId" ...}]
  ;
  ; @return (namespaced maps in vector)
  ; [{:namespace/id (string)}]
  ([collection-path documents]
   (save-documents! collection-path documents {}))

  ([collection-path documents options]
   (vector/->items documents #(save-document! collection-path % options))))

;; -- Updating document -------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn update-document!
  ; @description
  ; - Updates a document in a collection found by the given query.
  ; - You can apply custom functions for preparing and prototyping the document.
  ; - Returns a boolean indicating whether the update was successful.
  ;
  ; @param (string) collection-path
  ; @param (map) query
  ; {:namespace/id (string)(opt)}
  ; @param (map or namespaced map) document
  ; No need to be a namespaced map if using a prototype function that converts it
  ; into a namespaced form!
  ; @param (map)(opt) options
  ; {:prepare-f (function)(opt)
  ;   Applied on the input document.
  ;  :prototype-f (function)(opt)
  ;   Applied on the input document before any checking or preparing.
  ;   Must return a namespaced map!}
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
  ([collection-path query document]
   (update-document! collection-path query document {}))

  ([collection-path query document options]
   ; XXX#7100
   (boolean (if-let [document (as-> document % (actions.prototyping/update-input collection-path % options)
                                               (actions.checking/update-input %)
                                               (actions.preparing/update-input collection-path % options)
                                               (actions.adaptation/update-input %))]
                    (if-let [query (-> query reader.checking/find-query reader.adaptation/find-query)]
                            (let [result (actions.side-effects/update! collection-path query document {:multi false :upsert false})]
                                 (mrt/updated-existing? result)))))))

;; -- Updating documents ------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn update-documents!
  ; @description
  ; - Updates multiple documents in a collection found by the given query.
  ; - You can apply custom functions for preparing and prototyping each document.
  ; - Returns a boolean indicating whether the update was successful.
  ;
  ; @param (string) collection-path
  ; @param (map) query
  ; {:namespace/id (string)(opt)}
  ; @param (namespaced map) document
  ; No need to be a namespaced map if using a prototype function that converts it
  ; into a namespaced form!
  ; @param (map)(opt) options
  ; {:prepare-f (function)(opt)
  ;   Applied on each input document.
  ;  :prototype-f (function)(opt)
  ;   Applied on each input document before any checking or preparing.
  ;   Must return a namespaced map!}
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
  ([collection-path query document]
   (update-documents! collection-path query document {}))

  ([collection-path query document options]
   ; XXX#7100
   (boolean (if-let [document (as-> document % (actions.prototyping/update-input collection-path % options)
                                               (actions.checking/update-input %)
                                               (actions.preparing/update-input collection-path % options)
                                               (actions.adaptation/update-input %))]
                    (if-let [query (-> query reader.checking/find-query reader.adaptation/find-query)]
                            ; WARNING! DO NOT USE!
                            ; java.lang.IllegalArgumentException: Replacements can not be multi
                            (let [result (actions.side-effects/update! collection-path query document {:multi true :upsert false})]
                                 (mrt/updated-existing? result)))))))

;; -- Upserting document ------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn upsert-document!
  ; @description
  ; - Updates or inserts a document in or into a collection found by the given query.
  ; - You can apply custom functions for preparing and prototyping the document.
  ; - Returns a boolean indicating whether the updating/inserting was successful.
  ;
  ; @param (string) collection-path
  ; @param (map) query
  ; @param (map or namespaced map) document
  ; No need to be a namespaced map if using a prototype function that converts it
  ; into a namespaced form!
  ; @param (map)(opt) options
  ; {:ordered? (boolean)(opt)
  ;   Set to TRUE when upserting a document into an ordered collection!
  ;   Default: false
  ;  :prepare-f (function)(opt)
  ;   Applied on the input document.
  ;  :prototype-f (function)(opt)
  ;   Applied on the input document before any checking or preparing.
  ;   Must return a namespaced map!}
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
  ([collection-path query document]
   (upsert-document! collection-path query document {}))

  ([collection-path query document options]
   ; XXX#7100
   (boolean (if-let [document (as-> document % (actions.prototyping/upsert-input collection-path % options)
                                               (actions.checking/upsert-input %)
                                               (actions.preparing/upsert-input collection-path % options)
                                               (actions.adaptation/upsert-input %))]
                    (if-let [query (-> query reader.checking/find-query reader.adaptation/find-query)]
                            (let [result (actions.side-effects/upsert! collection-path query document {:multi false})]
                                 (mrt/acknowledged? result)))))))

;; -- Upserting documents -----------------------------------------------------
;; ----------------------------------------------------------------------------

(defn upsert-documents!
  ; @description
  ; - Updates or inserts multiple documents in or into a collection found by the given query.
  ; - You can apply custom functions for preparing and prototyping each document.
  ; - Returns a boolean indicating whether the updating/inserting was successful.
  ;
  ; @param (string) collection-path
  ; @param (map) query
  ; @param (namespaced map) document
  ; No need to be a namespaced map if using a prototype function that converts it
  ; into a namespaced form!
  ; @param (map)(opt) options
  ; {:ordered? (boolean)(opt)
  ;   Set to TRUE when upserting documents into an ordered collection!
  ;  :prepare-f (function)(opt)
  ;   Applied on each input document.
  ;  :prototype-f (function)(opt)
  ;   Applied on each input document before any checking or preparing.
  ;   Must return a namespaced map!}
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
  ([collection-path query document]
   (upsert-documents! collection-path query document {}))

  ([collection-path query document options]
   ; XXX#7100
   (boolean (if-let [document (as-> document % (actions.prototyping/upsert-input collection-path % options)
                                               (actions.checking/upsert-input %)
                                               (actions.preparing/upsert-input collection-path % options)
                                               (actions.adaptation/upsert-input %))]
                    (if-let [query (-> query reader.checking/find-query reader.adaptation/find-query)]
                            ; WARNING! DO NOT USE!
                            ; java.lang.IllegalArgumentException: Replacements can not be multi
                            (let [result (actions.side-effects/upsert! collection-path query document {:multi true})]
                                 (mrt/acknowledged? result)))))))

;; -- Applying on document ----------------------------------------------------
;; ----------------------------------------------------------------------------

(defn apply-on-document!
  ; @description
  ; - Applies the given function on a document.
  ; - You can apply custom functions for preparing and postparing the document.
  ; - Returns the updated document.
  ;
  ; @param (string) collection-path
  ; @param (string) document-id
  ; @param (function) f
  ; @param (map)(opt) options
  ; {:postpare-f (function)(opt)
  ;   Applied on the input document AFTER the passed 'f' function is applied.
  ;  :prepare-f (function)(opt)
  ;   Applied on the input document BEFORE the passed 'f' function is applied.}
  ;
  ; @usage
  ; (apply-on-document! "my_collection" "MyObjectId" #(assoc % :namespace/color "Blue") {...})
  ;
  ; @return (namespaced map)
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

;; -- Applying on collection --------------------------------------------------
;; ----------------------------------------------------------------------------

(defn apply-on-collection!
  ; @description
  ; - Applies the given function on every document in a collection.
  ; - You can apply custom functions for preparing and postparing each document.
  ; - Returns the updated documents in a vector.
  ;
  ; @param (string) collection-path
  ; @param (function) f
  ; @param (map)(opt) options
  ; {:postpare-f (function)(opt)
  ;   Applied on each input document AFTER the passed 'f' function is applied.
  ;  :prepare-f (function)(opt)
  ;   Applied on each input document BEFORE the passed 'f' function is applied.}
  ;
  ; @usage
  ; (apply-on-collection! "my_collection" #(assoc % :namespace/color "Blue") {...})
  ;
  ; @return (namespaced maps in vector)
  ([collection-path f]
   (apply-on-collection! collection-path f {}))

  ([collection-path f options]
   ; XXX#9801
   (if-let [collection (reader.engine/get-collection collection-path)]
           (letfn [(fi [result document]
                       (if-let [document (f document)]
                               (let [document (save-document! collection-path document options)]
                                    (conj result document))
                               (-> result)))]
                  (reduce fi [] collection)))))

;; -- Removing document -------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn- remove-unordered-document!
  ; @ignore
  ;
  ; @description
  ; - Removes a document from an unordered collection based on its ID.
  ; - Returns the document ID if the removal was successful.
  ;
  ; @param (string) collection-path
  ; @param (string) document-id
  ; @param (map) options
  ;
  ; @return (string)
  [collection-path document-id _]
  (if-let [document-id (actions.adaptation/document-id-input document-id)]
          (let [result (actions.side-effects/remove-by-id! collection-path document-id)]
               (if (mrt/acknowledged? result)
                   (actions.adaptation/document-id-output document-id)))))

(defn- remove-ordered-document!
  ; @ignore
  ;
  ; @description
  ; - Removes a document from an ordered collection based on its ID.
  ; - Returns the document ID if the removal was successful.
  ;
  ; @param (string) collection-path
  ; @param (string) document-id
  ; @param (map) options
  ;
  ; @return (string)
  [collection-path document-id _]
  (if-let [document-id (actions.adaptation/document-id-input document-id)]
          (do (let [document-id (actions.adaptation/document-id-output document-id)]
                   (reorder-following-documents! collection-path document-id {:operation :decrease}))
              (let [result (actions.side-effects/remove-by-id! collection-path document-id)]
                   (if (mrt/acknowledged? result)
                       (actions.adaptation/document-id-output document-id))))))

(defn remove-document!
  ; @description
  ; - Removes a document from a collection.
  ; - Returns the document ID if the removal was successful.
  ;
  ; @param (string) collection-path
  ; @param (string) document-id
  ; @param (map)(opt) options
  ; {:ordered? (boolean)
  ;   Set to TRUE when removing a document from an ordered collection!
  ;   Default: false}
  ;
  ; @usage
  ; (remove-document "my_collection" "MyObjectId" {...})
  ;
  ; @example
  ; (remove-document "my_collection" "MyObjectId" {...})
  ; =>
  ; "MyObjectId"
  ;
  ; @return (string)
  ([collection-path document-id]
   (remove-document! collection-path document-id {}))

  ([collection-path document-id {:keys [ordered?] :as options}]
   (if ordered? (remove-ordered-document!   collection-path document-id options)
                (remove-unordered-document! collection-path document-id options))))

;; -- Removing documents ------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn remove-documents!
  ; @description
  ; - Removes multiple documents from a collection.
  ; - Returns the document IDs in a vector if the removal was successful.
  ;
  ; @param (string) collection-path
  ; @param (strings in vector) document-ids
  ; @param (map)(opt) options
  ; {:ordered? (boolean)
  ;   Set to TRUE when removing documents from an ordered collection!
  ;   Default: false}
  ;
  ; @usage
  ; (remove-documents! "my_collection" ["MyObjectId" "YourObjectId"] {...})
  ;
  ; @example
  ; (remove-documents! "my_collection" ["MyObjectId" "YourObjectId"] {...})
  ; =>
  ; ["MyObjectId" "YourObjectId"]
  ;
  ; @return (strings in vector)
  ([collection-path document-ids]
   (remove-documents! collection-path document-ids {}))

  ([collection-path document-ids options]
   (vector/->items document-ids #(remove-document! collection-path % options))))

;; -- Removing documents ------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn remove-all-documents!
  ; @description
  ; - Removes all documents from a collection.
  ; - Returns the document IDs (?) in a vector if the removal was successful.
  ;
  ; @param (string) collection-path
  ;
  ; @usage
  ; (remove-all-documents! "my_collection")
  ;
  ; @return (?)
  [collection-path]
  (actions.side-effects/drop! collection-path))

;; -- Duplicating document ----------------------------------------------------
;; ----------------------------------------------------------------------------

(defn- duplicate-unordered-document!
  ; @ignore
  ;
  ; @description
  ; - Duplicates a document in an unordered collection.
  ; - Returns the copy document if the duplicating was successful.
  ;
  ; @param (string) collection-path
  ; @param (string) document-id
  ; @param (map) options
  ;
  ; @return (namespaced map)
  [collection-path document-id options]
  (if-let [document (reader.engine/get-document-by-id collection-path document-id)]
          (if-let [document-copy (actions.preparing/duplicate-input collection-path document options)]
                  (if-let [document-copy (actions.postparing/duplicate-input collection-path document-copy options)]
                          (if-let [document-copy (actions.adaptation/duplicate-input document-copy)]
                                  (let [result (actions.side-effects/insert-and-return! collection-path document-copy)]
                                       (actions.adaptation/duplicate-output result)))))))

(defn- duplicate-ordered-document!
  ; @ignore
  ;
  ; @description
  ; - Duplicates a document in an ordered collection.
  ; - Returns the copy document if the duplicating was successful.
  ;
  ; @param (string) collection-path
  ; @param (string) document-id
  ; @param (map) options
  ;
  ; @return (namespaced map)
  [collection-path document-id options]
  (if-let [document (reader.engine/get-document-by-id collection-path document-id)]
          (if-let [document-copy (actions.preparing/duplicate-input collection-path document options)]
                  (if-let [document-copy (actions.postparing/duplicate-input collection-path document-copy options)]
                          (if-let [document-copy (actions.adaptation/duplicate-input document-copy)]
                                  (do (reorder-following-documents! collection-path document-id {:operation :increase})
                                      (let [result (actions.side-effects/insert-and-return! collection-path document-copy)]
                                           (actions.adaptation/duplicate-output result))))))))

(defn duplicate-document!
  ; @description
  ; - Duplicates a document in a collection.
  ; - Returns the copy document if the duplicating was successful.
  ;
  ; @param (string) collection-path
  ; @param (string) document-id
  ; @param (map)(opt) options
  ; {:changes (namespaced map)(opt)
  ;   The copy document will include the provided changes.
  ;  :label-key (namespaced keyword)(opt)
  ;   Which key of the copy document gets the copy marker ("#2", "#3", etc.)
  ;   appended to its value.
  ;  :ordered? (boolean)(opt)
  ;   Set to TRUE when duplicating a document in an ordered collection!
  ;   Default: false
  ;  :postpare-f (function)(opt)
  ;   Applied on the copy document.
  ;  :prepare-f (function)(opt)
  ;   Applied on the copy document after it is derived from the original document.}
  ;
  ; @usage
  ; (duplicate-document! "my_collection" "MyObjectId" {...})
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
  ([collection-path document-id]
   (duplicate-document! collection-path document-id {}))

  ([collection-path document-id {:keys [ordered?] :as options}]
   (if ordered? (duplicate-ordered-document!   collection-path document-id options)
                (duplicate-unordered-document! collection-path document-id options))))

;; -- Duplicating documents ---------------------------------------------------
;; ----------------------------------------------------------------------------

(defn duplicate-documents!
  ; @description
  ; - Duplicates multiple documents in a collection.
  ; - Returns the copy documents in a vector if the duplicating was successful.
  ;
  ; @param (string) collection-path
  ; @param (strings in vector) document-ids
  ; @param (map)(opt) options
  ; {:label-key (namespaced keyword)(opt)
  ;   Which key of the copy documents gets the copy marker ("#2", "#3", etc.)
  ;   appended to its value.
  ;  :ordered? (boolean)(opt)
  ;   Set to TRUE when duplicating documents in an ordered collection!
  ;   Default: false
  ;  :postpare-f (function)(opt)
  ;   Applied on each copy document.
  ;  :prepare-f (function)(opt)
  ;   Applied on each copy document after they are derived from the original documents.
  ;  :prototype-f (function)(opt)
  ;   Applied on each input document before any checking or preparing.
  ;   Must return a namespaced map!}
  ;
  ; @usage
  ; (duplicate-documents! "my_collection" ["MyObjectId" "YourObjectId"] {...})
  ;
  ; @example
  ; (duplicate-documents! "my_collection" ["MyObjectId" "YourObjectId"] {...})
  ; =>
  ; [{...} {...}]
  ;
  ; @return (namespaced maps in vector)
  ([collection-path document-ids]
   (duplicate-documents! collection-path document-ids {}))

  ([collection-path document-ids options]
   (vector/->items document-ids #(duplicate-document! collection-path % options))))

;; -- Reordering collection ---------------------------------------------------
;; ----------------------------------------------------------------------------

(defn reorder-documents!
  ; @description
  ; - Reorders documents in a collection.
  ; - Returns the (?).
  ;
  ; @param (string) collection-path
  ; @param (vectors in vector) document-order
  ; [[(string) document-id
  ;   (integer) document-dex]]
  ;
  ; @usage
  ; (reorder-documents "my_collection" [["MyObjectId" 1] ["YourObjectId" 2]])
  ;
  ; @return (vectors in vector)
  [collection-path document-order]
  ; WARNING
  ; What if a document got a new position that is still used by another document?
  (let [namespace (reader.engine/get-collection-namespace collection-path)
        order-key (keyword/add-namespace :order namespace)]
       (letfn [(f0 [[document-id document-dex]]
                   (if-let [document-id (actions.adaptation/document-id-input document-id)]
                           (let [result (actions.side-effects/update! collection-path {:_id document-id}
                                                                      {"$set" {order-key document-dex}})]
                                (if (mrt/acknowledged? result)
                                    (-> [document-id document-dex])))))]
              (vector/->items document-order f0))))


(ns mongo-db.core.utils
    (:import org.bson.types.ObjectId)
    (:require [fruits.keyword.api     :as keyword]
              [fruits.map.api         :as map]
              [fruits.string.api      :as string]
              [monger.conversion      :as mcv]
              [mongo-db.core.messages :as core.messages]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn operator?
  ; @ignore
  ; Checks whether a value is a keyword representing a MongoDB operator.
  ;
  ; @param (*) n
  ;
  ; @example
  ; (operator? :$or)
  ; =>
  ; true
  ;
  ; @return (boolean)
  [n]
  (and (-> n keyword?)
       (-> n string/second-character (= "$"))))

(defn DBObject->edn
  ; @ignore
  ;
  ; @description
  ; Converts the given 'n' MongoDB database object (DBObject) into EDN map.
  ;
  ; @param (DBObject) n
  ;
  ; @return (map)
  [n]
  (try (mcv/from-db-object n true)
       (catch Exception e (println e))))

;; -- Document ID -------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn generate-id
  ; @description
  ; Returns a randomly generated ObjectId for documents.
  ;
  ; @usage
  ; (generate-id)
  ;
  ; @return (string)
  []
  (str (ObjectId.)))

(defn assoc-id
  ; @ignore
  ;
  ; @description
  ; Generates a unique ObjectId and associates it to the document with the appropriate namespace.
  ;
  ; @param (map) n
  ;
  ; @example
  ; (assoc-id {:namespace/my-key "my-value"})
  ; =>
  ; {:namespace/id "MyObjectId" :namespace/my-key "my-value"}
  ;
  ; @return (map)
  [n]
  (if-let [namespace (map/namespace n)]
          (let [document-id (generate-id)]
               (assoc n (keyword/add-namespace :id namespace) document-id))
          (-> n)))

(defn dissoc-id
  ; @ignore
  ;
  ; @description
  ; Removes the ID associated with a document.
  ;
  ; @param (map) n
  ; {:namespace/id (string)(opt)}
  ;
  ; @example
  ; (dissoc-id {:namespace/id "MyObjectId" :namespace/my-key "my-value"})
  ; =>
  ; {:namespace/my-key "my-value"}
  ;
  ; @return (map)
  [n]
  (if-let [namespace (map/namespace n)]
          (dissoc n (keyword/add-namespace :id namespace))
          (->     n)))

(defn id->_id
  ; @ignore
  ;
  ; @description
  ; Transforms a map that contains an ID within a specified namespace into a map
  ; with an '_id' field. It optionally parses the ID value as an ObjectId object
  ; when the 'parse?' option is true.
  ;
  ; @param (map) n
  ; {:namespace/id (*)(opt)}
  ; @param (map)(opt) options
  ; {:parse? (boolean)(opt)
  ;   Default: false}
  ;
  ; @example
  ; (id->_id {:namespace/id 1})
  ; =>
  ; {:_id 1}
  ;
  ; @example
  ; (id->_id {:namespace/id "MyObjectId"} {:parse? true})
  ; =>
  ; {:_id #<ObjectId MyObjectId>}
  ;
  ; @return (map)
  ; {:_id (org.bson.types.ObjectId object)}
  ([n]
   (id->_id n {}))

  ([n {:keys [parse?]}]
   ; The n map (provided as a parameter) does not have to contain the ':namespace/id' key!
   (if-let [namespace (map/namespace n)]
           (let [id-key (keyword/add-namespace :id namespace)]
                (if-let [value (get n id-key)]
                        (if parse? (let [object-id (ObjectId. value)]
                                        (-> n (assoc  :_id object-id)
                                              (dissoc id-key)))
                                   (-> n (assoc  :_id value)
                                         (dissoc id-key)))
                        (-> n)))
           (-> n))))

(defn _id->id
  ; @ignore
  ;
  ; @description
  ; Transforms a map that contains an '_id' field, into a map with the ID value
  ; placed within a specified namespace. It optionally unparse the '_id' value
  ; as a string when the 'unparse?' option is true.
  ;
  ; @param (map) n
  ; {:_id (*)(opt)}
  ; @param (map)(opt) options
  ; {:unparse? (boolean)(opt)
  ;   Default: false}
  ;
  ; @example
  ; (_id->id {:_id #<ObjectId MyObjectId>} {:unparse? true})
  ; =>
  ; {:namespace/id "MyObjectId"}
  ;
  ; @return (map)
  ; {:namespace/id (string)}
  ([n]
   (_id->id n {}))

  ([n {:keys [unparse?]}]
   ; The given n map does not have to contain the :_id key!
   (if-let [namespace (map/namespace n)]
           (let [id-key (keyword/add-namespace :id namespace)]
                (if-let [value (get n :_id)]
                        (if unparse? (let [document-id (str value)]
                                          (-> n (assoc  id-key document-id)
                                                (dissoc :_id)))
                                     (-> n (assoc  id-key value)
                                           (dissoc :_id)))
                        (-> n)))
           (-> n))))

(defn id->>_id
  ; @ignore
  ;
  ; @description
  ; Recursively applies the 'id->_id' function on the given 'n' map.
  ;
  ; @param (*) n
  ; {:namespace/id (string)(opt)}
  ; @param (map)(opt) options
  ; {:parse? (boolean)(opt)
  ;   Default: false}
  ;
  ; @example
  ; (id->>_id {:$or [{...} {:namespace/id "MyObjectId"}]})
  ; =>
  ; {:$or [{...} {:_id #<ObjectId MyObjectId>}]}
  ;
  ; @return (map)
  ; {:_id (org.bson.types.ObjectId object)}
  ([n]
   (id->>_id n {}))

  ([n options]
   (cond (map?    n) (reduce-kv #(assoc %1 %2 (id->>_id %3 options)) {} (id->_id n options))
         (vector? n) (reduce    #(conj  %1    (id->>_id %2 options)) [] n)
         :return  n)))

;; -- Document order ----------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn document->order
  ; @ignore
  ;
  ; @description
  ; Extracts the order value from a document. It looks for the order value within
  ; the specified namespace and returns it as an integer.
  ;
  ; @param (namespaced map) document
  ;
  ; @example
  ; (document->order {:namespace/order 3})
  ; =>
  ; 3
  ;
  ; @return (integer)
  [document]
  (if-let [namespace (map/namespace document)]
          (get document (keyword/add-namespace :order namespace))
          (throw (Exception. core.messages/MISSING-NAMESPACE-ERROR))))

;; -- Query -------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn query<-namespace
  ; @description
  ; - Applies the given namespace to every key in the given query excluding keys that are operators.
  ; - It supports optional recursive application of the namespace to nested maps when the 'recur?' option is set to TRUE.
  ; - Using dot notation could lead to accidentally creating multi-namespaced keywords. Therefore, this function applies
  ;   the given namespace by simply prepending it to keys without changing the key's structure:
  ;
  ; (query<-namespace {:a/b.c/d.e/f "My string"} :my-namespace)
  ; =>
  ; {:my-namespace/a/b.c/d.e/f "My string"}
  ;
  ; Multi-namespaced keywords could be a problem in future versions of Clojure!
  ; https://clojuredocs.org/clojure.core/keyword
  ;
  ; @param (map) query
  ; @param (keyword) namespace
  ; @param (map)(opt) options
  ; {:recur? (boolean)(opt)
  ;   Default: false}
  ;
  ; @usage
  ; (query<-namespace {:id         "MyObjectId"
  ;                    :my-keyword :my-value
  ;                    :$or        [{:id "AnotherObjectId"}]}
  ;                   :my-namespace)
  ;
  ; @example
  ; (query<-namespace {:id         "MyObjectId"
  ;                    :my-keyword :my-value
  ;                    :$or        [{:id "AnotherObjectId"}]}
  ;                   :my-namespace)
  ; =>
  ; {:my-namespace/id         "MyObjectId"
  ;  :my-namespace/my-keyword :my-value
  ;  :$or                     [{:id "AnotherObjectId"}]}
  ;
  ; @example
  ; (query<-namespace {:id         "MyObjectId"
  ;                    :my-keyword :my-value
  ;                    :my-map     {:id "AnotherObjectId"}}
  ;                   :my-namespace
  ;                   {:recur? true})
  ; =>
  ; {:my-namespace/id         "MyObjectId"
  ;  :my-namespace/my-keyword :my-value
  ;  :my-namespace/my-map     {:my-namespace/id "AnotherObjectId"}}
  ;
  ; @return (namespaced map)
  ([query namespace]
   (query<-namespace query namespace {}))

  ([query namespace {:keys [recur?]}]
   (letfn [(f0 [k] (if (-> k operator?)
                       (-> k)

                       ; It prepends the given namespace to the key without changing
                       ; the key's structure, because it might be a multi-namespaced
                       ; keyword (using the dot notation could lead to multi-namespaced
                       ; keywords).
                       (as-> k % (str  %)
                                 (subs % 1)
                                 (str (name namespace) "/" %))))]

          (if recur? (map/->>keys query f0)
                     (map/->keys  query f0)))))

(defn apply-dot-notation
  ; @description
  ; - Takes a nested query map as input and flattens it by collapsing nested fields
  ;   into a flat map structure that corresponds to the dot notation.
  ; - It returns a new map where nested fields are represented using dot notation.
  ;
  ; https://www.mongodb.com/docs/manual/core/document/#dot-notation
  ;
  ; @param (map) query
  ;
  ; @usage
  ; (apply-dot-notation {:user {:id "MyObjectId"}})
  ;
  ; @example
  ; (apply-dot-notation {:user {:id "MyObjectId"}})
  ; =>
  ; {:user.id "MyObjectId"}
  ;
  ; @example
  ; (apply-dot-notation {:user {:id "MyObjectId"}
  ;                      :$or [{:user {:id "MyObjectId"}}]})
  ; =>
  ; {:user.id "MyObjectId"
  ;  :$or [{:user.id "MyObjectId"}]}
  ;
  ; @return (map)
  [query]
  (letfn [; The 'except-f' function provides exception rule for the 'map/collapse'
          ; function in order to avoid collapsing operator keys in the given query.
          (except-f [k _] (operator? k))]

         ; The 'map/collapse' function collapses the nested map structure in the
         ; given query to a flatten map structure where nested keys converted to
         ; flatten keys separated with '.' character.
         ;
         ; The output structure corresponds to the dot notation structure:
         ; (map/collapse {:a {:b {:c "My value"}}} {...})
         ; =>
         ; {:a.b.c "My value"}
         (map/collapse query {:keywordize? true :inner-except-f except-f :outer-except-f except-f :separator "."})))

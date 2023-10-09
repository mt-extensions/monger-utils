
(ns mongo-db.core.utils
    (:import  org.bson.types.ObjectId)
    (:require [keyword.api          :as keyword]
              [map.api              :as map]
              [monger.conversion    :as mcv]
              [mongo-db.core.errors :as core.errors]
              [noop.api             :refer [return]]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn operator?
  ; @ignore
  ; Checks wheter a value is a keyword representing a MongoDB operator.
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
  (and (keyword? n)
       ; In Java language only one character long string could be character types!
       (-> n str second str (= "$"))))

(defn DBObject->edn
  ; @ignore
  ;
  ; @description
  ; Converts a MongoDB database object (DBObject) into an EDN map.
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
  (if-let [namespace (map/get-namespace n)]
          (let [document-id (generate-id)]
               (assoc n (keyword/add-namespace :id namespace) document-id))
          (return n)))

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
  (if-let [namespace (map/get-namespace n)]
          (dissoc n (keyword/add-namespace :id namespace))
          (return n)))

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
   ; The n map (given as a parameter) doesn't have to contain the :namespace/id key!
   (if-let [namespace (map/get-namespace n)]
           (let [id-key (keyword/add-namespace :id namespace)]
                (if-let [value (get n id-key)]
                        (if parse? (let [object-id (ObjectId. value)]
                                        (-> n (assoc  :_id object-id)
                                              (dissoc id-key)))
                                   (-> n (assoc  :_id value)
                                         (dissoc id-key)))
                        (return n)))
           (return n))))

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
   ; The given n map doesn't have to contain the :_id key!
   (if-let [namespace (map/get-namespace n)]
           (let [id-key (keyword/add-namespace :id namespace)]
                (if-let [value (get n :_id)]
                        (if unparse? (let [document-id (str value)]
                                          (-> n (assoc  id-key document-id)
                                                (dissoc :_id)))
                                     (-> n (assoc  id-key value)
                                           (dissoc :_id)))
                        (return n)))
           (return n))))

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
  (if-let [namespace (map/get-namespace document)]
          (get document (keyword/add-namespace :order namespace))
          (throw (Exception. core.errors/MISSING-NAMESPACE-ERROR))))

;; -- Query -------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn query<-namespace
  ; @description
  ; Applies the given namespace to every key in the given query excluding keys that are operators.
  ; It supports optional recursive application of the namespace to nested maps when
  ; the 'recur?' option is true.
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
  ;                    :$or        [{:id "YourObjectId"}]}
  ;                   :my-namespace)
  ;
  ; @example
  ; (query<-namespace {:id         "MyObjectId"
  ;                    :my-keyword :my-value
  ;                    :$or        [{:id "YourObjectId"}]}
  ;                   :my-namespace)
  ; =>
  ; {:my-namespace/id         "MyObjectId"
  ;  :my-namespace/my-keyword :my-value
  ;  :$or                     [{:id "YourObjectId"}]}
  ;
  ; @example
  ; (query<-namespace {:id         "MyObjectId"
  ;                    :my-keyword :my-value
  ;                    :my-map     {:id "YourObjectId"}}
  ;                   :my-namespace
  ;                   {:recur? true})
  ; =>
  ; {:my-namespace/id         "MyObjectId"
  ;  :my-namespace/my-keyword :my-value
  ;  :my-namespace/my-map     {:my-namespace/id "YourObjectId"}}
  ;
  ; @return (namespaced map)
  ([query namespace]
   (query<-namespace query namespace {}))

  ([query namespace {:keys [recur?]}]
   (letfn [(f [k] (if (operator?             k)
                      (return                k)
                      (keyword/add-namespace k namespace)))]
          (if recur? (map/->>keys query f)
                     (map/->keys  query f)))))

(defn flatten-query
  ; @description
  ; Takes a query map as input and flattens it by collapsing nested fields into
  ; a flat map structure. It returns a new map where nested fields are represented
  ; using dot notation.
  ;
  ; @param (map) query
  ;
  ; @usage
  ; (flatten-query {:user {:id "MyObjectId"}})
  ;
  ; @example
  ; (flatten-query {:user {:id "MyObjectId"}})
  ; =>
  ; {:user.id "MyObjectId"}
  ;
  ; @example
  ; (flatten-query {:user {:id "MyObjectId"}
  ;                 :$or [{:user {:id "MyObjectId"}}]})
  ; =>
  ; {:user.id "MyObjectId"
  ;  :$or [{:user.id "MyObjectId"}]}
  ;
  ; @return (map)
  [query]
  (letfn [(except-f [k _] (operator? k))]
         (let [collapsed-query (map/collapse query {:keywordize? true :inner-except-f except-f :outer-except-f except-f})]
              (if-let [namespace (map/get-namespace query)]
                      (query<-namespace collapsed-query namespace {:recur? true})
                      (return           collapsed-query)))))

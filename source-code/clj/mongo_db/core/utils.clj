
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

(defn document?
  ; @ignore
  ;
  ; @param (*) n
  ;
  ; @example
  ; (document? {:namespace/my-key "..."})
  ; =>
  ; false
  ;
  ; @example
  ; (document? {:namespace/my-key "..."
  ;             :namespace/id     "..."})
  ; =>
  ; true
  ;
  ; @return (boolean)
  [n]
  (and (-> n map?)
       (if-let [namespace (map/get-namespace n)]
               (get n (keyword/add-namespace :id namespace)))))

(defn DBObject->edn
  ; @ignore
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
   ; The n map (given as a parameter) doesn't have to contain the :_id key!
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
         (vector? n) (reduce    #(conj  %1    (id->>_id %2 options)) []          n)
         :return  n)))

;; -- Document order ----------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn document->order
  ; @ignore
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
  ; Recursively applies the given namespace on every key in the given query
  ; except for operator keys.
  ;
  ; @param (map) query
  ; @param (keyword) namespace
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
  ; @usage
  ; {:my-namespace/id         "MyObjectId"
  ;  :my-namespace/my-keyword :my-value
  ;  :$or                     [{:my-namespace/id "YourObjectId"}]}
  ;
  ; @return (namespaced map)
  [query namespace]
  (letfn [(f [k] (if (operator?             k)
                     (return                k)
                     (keyword/add-namespace k namespace)))]
         (map/->>keys query f)))

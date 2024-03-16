
(ns mongo-db.core.utils
    (:import org.bson.types.ObjectId)
    (:require [fruits.keyword.api     :as keyword]
              [fruits.map.api         :as map]
              [fruits.string.api      :as string]
              [monger.conversion      :as mcv]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn operator?
  ; @ignore
  ; Checks whether the given value is a keyword representing a MongoDB operator.
  ;
  ; @param (*) n
  ;
  ; @usage
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
  ; Converts the given MongoDB database object (DBObject) into an EDN map.
  ;
  ; @param (DBObject) n
  ;
  ; @return (map)
  [n]
  (mcv/from-db-object n true))

;; -- Document ID -------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn generate-id
  ; @description
  ; Returns a randomly generated ObjectId string.
  ;
  ; @usage
  ; (generate-id)
  ; =>
  ; "MyObjectId"
  ;
  ; @return (string)
  []
  (-> (ObjectId.) str))

(defn assoc-id
  ; @ignore
  ;
  ; @description
  ; Associates a randomly generated ObjectId string to the given map.
  ;
  ; @param (map) n
  ;
  ; @usage
  ; (assoc-id {:namespace/my-key "my-value"})
  ; =>
  ; {:namespace/id "MyObjectId" :namespace/my-key "my-value"}
  ;
  ; @return (map)
  [n]
  (let [document-id (generate-id)]
       (if-let [namespace (map/namespace n)]
               (let [id-key (keyword/add-namespace :id namespace)] (assoc n id-key document-id))
               (let [id-key :id]                                   (assoc n id-key document-id)))))

(defn dissoc-id
  ; @ignore
  ;
  ; @description
  ; Removes the ID from the given map.
  ;
  ; @param (map) n
  ; {:namespace/id (*)(opt)}
  ;
  ; @usage
  ; (dissoc-id {:namespace/id "MyObjectId" ...})
  ; =>
  ; {...}
  ;
  ; @return (map)
  [n]
  (if-let [namespace (map/namespace n)]
          (let [id-key (keyword/add-namespace :id namespace)] (dissoc n id-key))
          (let [id-key :id]                                   (dissoc n id-key))))

(defn parse-id
  ; @ignore
  ;
  ; @description
  ; Parses the ID into an object within the given map.
  ;
  ; @param (map) n
  ; {:namespace/id (string)(opt)}
  ;
  ; @usage
  ; (parse-id {:namespace/id "MyObjectId"})
  ; =>
  ; {:namespace/id #<ObjectId MyObjectId>}
  ;
  ; @return (map)
  ; {:namespace/id (org.bson.types.ObjectId object)}
  [n]
  (if-let [namespace (map/namespace n)]
          (let [id-key (keyword/add-namespace :id namespace)] (map/update-some n id-key ObjectId.))
          (let [id-key :id]                                   (map/update-some n id-key ObjectId.))))

(defn unparse-id
  ; @ignore
  ;
  ; @description
  ; Unparses the ID into a string within the given map.
  ;
  ; @param (map) n
  ; {:namespace/id (org.bson.types.ObjectId object)(opt)}
  ;
  ; @usage
  ; (unparse-id {:namespace/id #<ObjectId MyObjectId>})
  ; =>
  ; {:namespace/id "MyObjectId"}
  ;
  ; @return (map)
  ; {:namespace/id (string)}
  [n]
  (if-let [namespace (map/namespace n)]
          (let [id-key (keyword/add-namespace :id namespace)] (map/update-some n id-key str))
          (let [id-key :id]                                   (map/update-some n id-key str))))

(defn id->_id
  ; @ignore
  ;
  ; @description
  ; Renames the ':namespace/id' key to ':_id' (a MongoDB compatible identifier) within the given map.
  ;
  ; @param (map) n
  ; {:namespace/id (*)(opt)}
  ;
  ; @usage
  ; (id->_id {:namespace/id "MyObjectId"})
  ; =>
  ; {:_id "MyObjectId"}
  ;
  ; @return (map)
  ; {:_id (*)}
  [n]
  (if-let [namespace (map/namespace n)]
          (let [id-key (keyword/add-namespace :id namespace)] (map/move-some n id-key :_id))
          (let [id-key :id]                                   (map/move-some n id-key :_id))))

(defn _id->id
  ; @ignore
  ;
  ; @description
  ; Renames the ':_id' key to ':namespace/id' within the given map.
  ;
  ; @param (map) n
  ; {:_id (*)(opt)}
  ;
  ; @usage
  ; (_id->id {:_id "MyObjectId"})
  ; =>
  ; {:namespace/id "MyObjectId"}
  ;
  ; @return (map)
  ; {:namespace/id (*)}
  [n]
  (if-let [namespace (map/namespace n)]
          (let [id-key (keyword/add-namespace :id namespace)] (map/move-some n :_id id-key))
          (let [id-key :id]                                   (map/move-some n :_id id-key))))

(defn walk
  ; @ignore
  ;
  ; @description
  ; Applies the given function on the given map and also on every nested map within.
  ;
  ; @param (map) n
  ; @param (function) f
  ;
  ; @usage
  ; (walk {...} id->_id)
  ;
  ; @return (map)
  [n f]
  (cond (map?    n) (reduce-kv #(assoc %1 %2 (walk %3)) {} (f n))
        (vector? n) (reduce    #(conj  %1    (walk %2)) [] n)
        :return  n))

(defn id->>_id
  ; @ignore
  ;
  ; @description
  ; Recursively applies the 'id->_id' function on the given map.
  ;
  ; @param (*) n
  ; {:namespace/id (*)(opt)}
  ;
  ; @usage
  ; (id->>_id {:$or [{...} {:namespace/id "MyObjectId"}]})
  ; =>
  ; {:$or [{...} {:_id "MyObjectId"}]}
  ;
  ; @return (map)
  ; {:_id (*)}
  [n]
  (cond (map?    n) (reduce-kv #(assoc %1 %2 (id->>_id %3)) {} (id->_id n))
        (vector? n) (reduce    #(conj  %1    (id->>_id %2)) [] n)
        :return  n))

(defn _id->>id
  ; @ignore
  ;
  ; @description
  ; Recursively applies the '_id->id' function on the given map.
  ;
  ; @param (*) n
  ; {:_id (*)(opt)}
  ;
  ; @usage
  ; (_id->>id {:$or [{...} {:_id "MyObjectId"}]})
  ; =>
  ; {:$or [{...} {:namespace/id "MyObjectId"}]}
  ;
  ; @return (map)
  ; {:namespace/id (*)}
  [n]
  (cond (map?    n) (reduce-kv #(assoc %1 %2 (_id->>id %3)) {} (_id->id n))
        (vector? n) (reduce    #(conj  %1    (_id->>id %2)) [] n)
        :return  n))

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

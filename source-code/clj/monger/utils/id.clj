
(ns monger.utils.id
    (:import org.bson.types.ObjectId)
    (:require [fruits.keyword.api     :as keyword]
              [fruits.map.api         :as map]))

;; ----------------------------------------------------------------------------
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
  ; @description
  ; Associates a randomly generated ObjectId string to the given map.
  ;
  ; @param (map) n
  ;
  ; @usage
  ; (assoc-id {...})
  ; =>
  ; {:my-namespace/id "MyObjectId"
  ;  ...}
  ;
  ; @return (map)
  [n]
  (let [document-id (generate-id)]
       (if-let [namespace (map/namespace n)]
               (let [id-key (keyword/add-namespace :id namespace)] (assoc n id-key document-id))
               (let [id-key :id]                                   (assoc n id-key document-id)))))

(defn dissoc-id
  ; @description
  ; Removes the ID from the given map.
  ;
  ; @param (map) n
  ; {:my-namespace/id (*)(opt)}
  ;
  ; @usage
  ; (dissoc-id {:my-namespace/id "MyObjectId" ...})
  ; =>
  ; {...}
  ;
  ; @return (map)
  [n]
  (if-let [namespace (map/namespace n)]
          (let [id-key (keyword/add-namespace :id namespace)] (dissoc n id-key))
          (let [id-key :id]                                   (dissoc n id-key))))

(defn parse-id
  ; @description
  ; Parses the ID string into an object within the given map.
  ;
  ; @param (map) n
  ; {:my-namespace/id (string)(opt)}
  ;
  ; @usage
  ; (parse-id {:my-namespace/id "MyObjectId"})
  ; =>
  ; {:my-namespace/id #<ObjectId MyObjectId>}
  ;
  ; @return (map)
  ; {:my-namespace/id (org.bson.types.ObjectId object)}
  [n]
  (if-let [namespace (map/namespace n)]
          (let [id-key (keyword/add-namespace :id namespace)] (map/update-some n id-key ObjectId.))
          (let [id-key :id]                                   (map/update-some n id-key ObjectId.))))

(defn unparse-id
  ; @description
  ; Unparses the ID object into a string within the given map.
  ;
  ; @param (map) n
  ; {:my-namespace/id (org.bson.types.ObjectId object)(opt)}
  ;
  ; @usage
  ; (unparse-id {:my-namespace/id #<ObjectId MyObjectId>})
  ; =>
  ; {:my-namespace/id "MyObjectId"}
  ;
  ; @return (map)
  ; {:my-namespace/id (string)}
  [n]
  (if-let [namespace (map/namespace n)]
          (let [id-key (keyword/add-namespace :id namespace)] (map/update-some n id-key str))
          (let [id-key :id]                                   (map/update-some n id-key str))))

(defn adapt-id
  ; @description
  ; Renames the ':my-namespace/id' key to ':_id' (a MongoDB compatible identifier) within the given map.
  ;
  ; @param (map) n
  ; {:my-namespace/id (*)(opt)}
  ;
  ; @usage
  ; (adapt-id {:my-namespace/id "MyObjectId"})
  ; =>
  ; {:_id "MyObjectId"}
  ;
  ; @return (map)
  ; {:_id (*)}
  [n]
  (if-let [namespace (map/namespace n)]
          (let [id-key (keyword/add-namespace :id namespace)] (map/move-some n id-key :_id))
          (let [id-key :id]                                   (map/move-some n id-key :_id))))

(defn normalize-id
  ; @description
  ; Renames the ':_id' key to ':my-namespace/id' within the given map.
  ;
  ; @param (map) n
  ; {:_id (*)(opt)}
  ;
  ; @usage
  ; (_id->id {:_id "MyObjectId"})
  ; =>
  ; {:my-namespace/id "MyObjectId"}
  ;
  ; @return (map)
  ; {:my-namespace/id (*)}
  [n]
  (if-let [namespace (map/namespace n)]
          (let [id-key (keyword/add-namespace :id namespace)] (map/move-some n :_id id-key))
          (let [id-key :id]                                   (map/move-some n :_id id-key))))

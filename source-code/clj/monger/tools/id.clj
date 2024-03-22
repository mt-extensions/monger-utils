
(ns monger.tools.id
    (:import org.bson.types.ObjectId))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn generate-id
  ; @description
  ; Returns a randomly generated ObjectId string optionally parsed into an object.
  ;
  ; @param (map)(opt) options
  ; {:parse-id? (boolean)(opt)
  ;   Default: false}
  ;
  ; @usage
  ; (generate-id)
  ; =>
  ; "MyObjectId"
  ;
  ; @usage
  ; (generate-id {:parse-id? true})
  ; =>
  ; #<ObjectId MyObjectId>
  ;
  ; @return (org.bson.types.ObjectId object or string)
  ([]
   (generate-id {}))

  ([{:keys [parse-id?]}]
   (if parse-id? (-> (ObjectId.))
                 (-> (ObjectId.) str))))

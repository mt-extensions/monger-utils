
(ns monger.stages.acknowledging
    (:require [monger.result]
              [fruits.vector.api :as vector]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn result-acknowledged?
  ; @description
  ; Returns TRUE if the given result corresponds to a successful action,
  ; otherwise throws an error (if ':e' is provided).
  ;
  ; @param (com.mongodb.WriteResult object) result
  ; @param (map)(opt) options
  ; {:e (string)(opt)}
  ;
  ; @usage
  ; (result-acknowledged? ? {:e "Something happened!"})
  ; =>
  ; true
  ;
  ; @return (boolean)
  ([result]
   (result-acknowledged? result {}))

  ([result {:keys [e]}]
   (boolean (if e (if (-> result some?) (or (monger.result/acknowledged? result)
                                            (throw (ex-info e {:result result})))
                                        (or (throw (ex-info e {:result result}))))
                  (if (-> result some?)     (monger.result/acknowledged? result))))))

(defn results-acknowledged?
  ; @description
  ; Applies the 'result-acknowledged?' function on the given results.
  ;
  ; @param (com.mongodb.WriteResult objects in vector) results
  ; @param (map)(opt) options
  ; {:e (string)(opt)}
  ;
  ; @usage
  ; (results-acknowledged? [?] {:e "Something happened!"})
  ; =>
  ; [true]
  ;
  ; @return (booleans in vector)
  ([results]
   (results-acknowledged? results {}))

  ([results options]
   (letfn [(f0 [%] (result-acknowledged? % options))]
          (-> results (vector/->items f0)))))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn updated-existing?
  ; @description
  ; Returns TRUE if the given result corresponds to a successful update action,
  ; otherwise throws an error (if ':e' is provided).
  ;
  ; @param (com.mongodb.WriteResult object) result
  ; @param (map)(opt) options
  ; {:e (string)(opt)}
  ;
  ; @usage
  ; (updated-existing? ? {:e "Something happened!"})
  ; =>
  ; true
  ;
  ; @return (boolean)
  ([result]
   (updated-existing? result {}))

  ([result {:keys [e]}]
   (boolean (if e (if (-> result some?) (or (monger.result/updated-existing? result)
                                            (throw (ex-info e {:result result})))
                                        (or (throw (ex-info e {:result result}))))
                  (if (-> result some?)     (monger.result/updated-existing? result))))))

(defn updated-existings?
  ; @description
  ; Applies the 'updated-existing?' function on the given results.
  ;
  ; @param (com.mongodb.WriteResult objects in vector) result
  ; @param (map)(opt) options
  ; {:e (string)(opt)}
  ;
  ; @usage
  ; (updated-existings? [?] {:e "Something happened!"})
  ; =>
  ; [true]
  ;
  ; @return (booleans in vector)
  ([results]
   (updated-existings? results {}))

  ([results options]
   (letfn [(f0 [%] (updated-existing? % options))]
          (-> results (vector/->items f0)))))


(ns monger.stages.id
    (:require [monger.tools.api :as monger.tools]
              [fruits.vector.api :as vector]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn assoc-document-id
  ; @description
  ; Associates a randomly generate ObjectId string as document ID to the given document (if ':id-key' is provided).
  ;
  ; @param (map) document
  ; @param (map)(opt) options
  ; {:id-key (keyword)(opt)}
  ;
  ; @usage
  ; (assoc-document-id {...}
  ;                    {:id-key :my-id})
  ; =>
  ; {:my-namespace/my-id "MyObjectId" ...}
  ;
  ; @return (map)
  ([document]
   (assoc-document-id document {}))

  ([document {:keys [id-key]}]
   (if (-> document some?)
       (if id-key (let [document-id (monger.tools/generate-id)]
                       (-> document (monger.tools/assoc-value id-key document-id)))
                  (-> document)))))

(defn assoc-documents-id
  ; @description
  ; Applies the 'assoc-document-id' function on the given documents.
  ;
  ; @param (maps in vector) documents
  ; @param (map)(opt) options
  ; {:id-key (keyword)(opt)}
  ;
  ; @usage
  ; (assoc-documents-id [{...}]
  ;                     {:id-key :my-id})
  ; =>
  ; [{:my-namespace/my-id "MyObjectId" ...}]
  ;
  ; @return (maps in vector)
  ([documents]
   (assoc-documents-id documents {}))

  ([documents options]
   (letfn [(f0 [%] (assoc-document-id % options))]
          (if (-> documents some?)
              (-> documents (vector/->items f0))))))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn dissoc-document-id
  ; @description
  ; Dissociates the document ID from the given document (if ':id-key' is provided).
  ;
  ; @param (map) document
  ; @param (map)(opt) options
  ; {:id-key (keyword)(opt)}
  ;
  ; @usage
  ; (dissoc-document-id {:my-namespace/my-id "MyObjectId" ...}
  ;                     {:id-key :my-id})
  ; =>
  ; {...}
  ;
  ; @return (map)
  ([document]
   (dissoc-document-id document {}))

  ([document {:keys [id-key]}]
   (if (-> document some?)
       (if id-key (-> document (monger.tools/dissoc-value id-key))
                  (-> document)))))

(defn dissoc-documents-id
  ; @description
  ; Applies the 'dissoc-document-id' function on the given documents.
  ;
  ; @param (maps in vector) documents
  ; @param (map)(opt) options
  ; {:id-key (keyword)(opt)}
  ;
  ; @usage
  ; (dissoc-documents-id [{:my-namespace/my-id "MyObjectId" ...}]
  ;                      {:id-key :my-id})
  ; =>
  ; [{...}]
  ;
  ; @return (maps in vector)
  ([document]
   (dissoc-documents-id document {}))

  ([documents options]
   (letfn [(f0 [%] (dissoc-document-id % options))]
          (if (-> documents some?)
              (-> documents (vector/->items f0))))))

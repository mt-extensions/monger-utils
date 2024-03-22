
(ns monger.stages.checking
    (:require [fruits.map.api :as map]
              [fruits.vector.api :as vector]
              [monger.stages.messages :as messages]
              [monger.tools.api :as monger.tools]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn check-database-name
  ; @description
  ; - Returns the given database name if provided as a keyword.
  ; - Optionally throws an error if the given database name is not a keyword (if ':check-type?' is TRUE).
  ;
  ; @param (*) database-name
  ; @param (map)(opt) options
  ; {:check-type? (boolean)(opt)}
  ;
  ; @usage
  ; (check-database-name :my-database)
  ; =>
  ; :my-database
  ;
  ; @return (*)
  ([database-name]
   (check-database-name database-name {}))

  ([database-name {:keys [check-type?]}]
   (cond (-> database-name keyword? not (and check-type?)) (-> messages/DATABASE-NAME-TYPE-ERROR (ex-info {:database-name database-name}) throw)
         (-> database-name keyword?)                       (-> database-name))))

(defn check-databases-name
  ; @description
  ; - Applies the 'check-database-name' function on the given database names.
  ; - Options (excluding ':e') are passed to the 'check-database-name' function.
  ; - Optionally throws an error if no database name is provided (if ':e' is provided).
  ;
  ; @param (vector) database-names
  ; @param (map)(opt) options
  ; {:check-type? (boolean)(opt)
  ;  :e (string)(opt)}
  ;
  ; @usage
  ; (check-databases-name [:my-database :another-database])
  ; =>
  ; [:my-database :another-database]
  ;
  ; @return (vector)
  ([database-names]
   (check-databases-name database-names {}))

  ([database-names {:keys [e] :as options}]
   (letfn [(f0 [%] (check-database-name % options))]
          (cond (-> database-names vector/not-empty?) (-> database-names (vector/->items f0))
                (-> e some?)                          (-> e (ex-info {:database-names database-names}) throw)
                :return database-names))))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn check-collection-name
  ; @description
  ; - Returns the given collection name if provided as a keyword.
  ; - Optionally throws an error if the given collection name is not a keyword (if ':check-type?' is TRUE).
  ;
  ; @param (*) collection-name
  ; @param (map)(opt) options
  ; {:check-type? (boolean)(opt)}
  ;
  ; @usage
  ; (check-collection-name :my-collection)
  ; =>
  ; :my-collection
  ;
  ; @return (*)
  ([collection-name]
   (check-collection-name collection-name {}))

  ([collection-name {:keys [check-type?]}]
   (cond (-> collection-name keyword? not (and check-type?)) (-> messages/COLLECTION-NAME-TYPE-ERROR (ex-info {:collection-name collection-name}) throw)
         (-> collection-name keyword?)                       (-> collection-name))))

(defn check-collections-name
  ; @description
  ; - Applies the 'check-collection-name' function on the given collection names.
  ; - Options (excluding ':e') are passed to the 'check-collection-name' function.
  ; - Optionally throws an error if no collection name is provided (if ':e' is provided).
  ;
  ; @param (vector) collection-names
  ; @param (map)(opt) options
  ; {:check-type? (boolean)(opt)
  ;  :e (string)(opt)}
  ;
  ; @usage
  ; (check-collections-name [:my-collection :another-collection])
  ; =>
  ; [:my-collection :another-collection]
  ;
  ; @return (vector)
  ([collection-names]
   (check-collections-name collection-names {}))

  ([collection-names {:keys [e] :as options}]
   (letfn [(f0 [%] (check-collection-name % options))]
          (cond (-> collection-names vector/not-empty?) (-> collection-names (vector/->items f0))
                (-> e some?)                            (-> e (ex-info {:collection-names collection-names}) throw)
                :return collection-names))))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn check-collection-namespace
  ; @description
  ; - Returns the given collection namespace if provided as a keyword.
  ; - Optionally throws an error if the given collection namespace is not a keyword (if ':check-type?' is TRUE).
  ;
  ; @param (*) collection-namespace
  ; @param (map)(opt) options
  ; {:check-type? (boolean)(opt)}
  ;
  ; @usage
  ; (check-collection-namespace :my-collection)
  ; =>
  ; :my-collection
  ;
  ; @return (*)
  ([collection-namespace]
   (check-collection-namespace collection-namespace {}))

  ([collection-namespace {:keys [check-type?]}]
   (cond (-> collection-namespace keyword? not (and check-type?)) (-> messages/COLLECTION-NAMESPACE-TYPE-ERROR (ex-info {:collection-namespace collection-namespace}) throw)
         (-> collection-namespace keyword?)                       (-> collection-namespace))))

(defn check-collections-namespace
  ; @description
  ; - Applies the 'check-collection-namespace' function on the given collection namespaces.
  ; - Options (excluding ':e') are passed to the 'check-collection-namespace' function.
  ; - Optionally throws an error if no collection namespace is provided (if ':e' is provided).
  ;
  ; @param (vector) collection-namespaces
  ; @param (map)(opt) options
  ; {:check-type? (boolean)(opt)
  ;  :e (string)(opt)}
  ;
  ; @usage
  ; (check-collections-namespace [:my-collection :another-collection])
  ; =>
  ; [:my-collection :another-collection]
  ;
  ; @return (vector)
  ([collection-namespaces]
   (check-collections-namespace collection-namespaces {}))

  ([collection-namespaces {:keys [e] :as options}]
   (letfn [(f0 [%] (check-collection-namespace % options))]
          (cond (-> collection-namespaces vector/not-empty?) (-> collection-namespaces (vector/->items f0))
                (-> e some?)                                 (-> e (ex-info {:collection-namespaces collection-namespaces}) throw)
                :return collection-namespaces))))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn check-locale
  ; @description
  ; - Returns the given locale if provided as a keyword.
  ; - Optionally throws an error if the given locale is not a keyword (if ':check-type?' is TRUE).
  ;
  ; @param (*) locale
  ; @param (map)(opt) options
  ; {:check-type? (boolean)(opt)}
  ;
  ; @usage
  ; (check-locale :my-locale)
  ; =>
  ; :my-locale
  ;
  ; @return (*)
  ([locale]
   (check-locale locale {}))

  ([locale {:keys [check-type?]}]
   (cond (-> locale keyword? not (and check-type?)) (-> messages/LOCALE-TYPE-ERROR (ex-info {:locale locale}) throw)
         (-> locale keyword?)                       (-> locale))))

(defn check-locales
  ; @description
  ; - Applies the 'check-locale' function on the given locales.
  ; - Options (excluding ':e') are passed to the 'check-locale' function.
  ; - Optionally throws an error if no locale is provided (if ':e' is provided).
  ;
  ; @param (vector) locales
  ; @param (map)(opt) options
  ; {:check-type? (boolean)(opt)
  ;  :e (string)(opt)}
  ;
  ; @usage
  ; (check-locale [:my-locale :another-locale])
  ; =>
  ; [:my-locale :another-locale]
  ;
  ; @return (vector)
  ([locales]
   (check-locales locales {}))

  ([locales {:keys [e] :as options}]
   (letfn [(f0 [%] (check-locale % options))]
          (cond (-> locales vector/not-empty?) (-> locales (vector/->items f0))
                (-> e some?)                   (-> e (ex-info {:locales locales}) throw)
                :return locales))))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn check-id
  ; @description
  ; - Returns the given ID if provided as an ObjectId object.
  ; - Optionally throws an error if the given ID is not an ObjectId object (if ':check-type?' is TRUE).
  ;
  ; @param (*) id
  ; @param (map)(opt) options
  ; {:check-type? (boolean)(opt)}
  ;
  ; @usage
  ; (check-id #<ObjectId MyObjectId>
  ;           {:check-type? true})
  ; =>
  ; #<ObjectId MyObjectId>
  ;
  ; @return (*)
  ([id]
   (check-id id {}))

  ([id {:keys [check-type?]}]
   (cond (-> id monger.tools/object-id? not (and check-type?)) (-> messages/ID-TYPE-ERROR (ex-info {:id id}) throw)
         (-> id monger.tools/object-id?)                       (-> id))))

(defn check-ids
  ; @description
  ; - Applies the 'check-id' function on the given IDs.
  ; - Options (excluding ':e') are passed to the 'check-id' function.
  ; - Optionally throws an error if no ID is provided (if ':e' is provided).
  ;
  ; @param (vector) ids
  ; @param (map)(opt) options
  ; {:check-type? (boolean)(opt)
  ;  :e (string)(opt)}
  ;
  ; @usage
  ; (check-ids [#<ObjectId MyObjectId>]
  ;            {:check-type? true})
  ; =>
  ; [#<ObjectId MyObjectId>]
  ;
  ; @return (vector)
  ([ids]
   (check-ids ids {}))

  ([ids {:keys [e] :as options}]
   (letfn [(f0 [%] (check-id % options))]
          (cond (-> ids vector/not-empty?) (-> ids (vector/->items f0))
                (-> e some?)               (-> e (ex-info {:ids ids}) throw)
                :return ids))))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn check-document
  ; @description
  ; - Returns the given document if provided as a map.
  ; - Optionally throws an error if the given document is an empty map (if ':check-empty?' is TRUE).
  ; - Optionally throws an error if the given document is not namespaced (if ':check-ns?' is TRUE).
  ; - Optionally throws an error if the given document is not a map (if ':check-type?' is TRUE).
  ;
  ; @param (*) document
  ; @param (map)(opt) options
  ; {:check-empty? (boolean)(opt)
  ;  :check-ns? (boolean)(opt)
  ;  :check-type? (boolean)(opt)}
  ;
  ; @usage
  ; (check-document {:my-namespace/my-key "My value" ...}
  ;                 {:check-ns? true})
  ; =>
  ; {:my-namespace/my-key "My value" ...}
  ;
  ; @return (*)
  ([document]
   (check-document document {}))

  ([document {:keys [check-empty? check-ns? check-type?]}]
   (cond (-> document map/empty?          (and check-empty?)) (-> messages/DOCUMENT-EMPTY-ERROR           (ex-info {:document document}) throw)
         (-> document map/namespaced? not (and check-ns?))    (-> messages/NAMESPACED-DOCUMENT-TYPE-ERROR (ex-info {:document document}) throw)
         (-> document map? not            (and check-type?))  (-> messages/DOCUMENT-TYPE-ERROR            (ex-info {:document document}) throw)
         (-> document map?)                                   (-> document))))

(defn check-documents
  ; @description
  ; - Applies the 'check-document' function on the given documents.
  ; - Options (excluding ':e') are passed to the 'check-document' function.
  ; - Optionally throws an error if no document is provided (if ':e' is provided).
  ;
  ; @param (vector) documents
  ; @param (map)(opt) options
  ; {:check-empty? (boolean)(opt)
  ;  :check-ns? (boolean)(opt)
  ;  :check-type? (boolean)(opt)
  ;  :e (string)(opt)}
  ;
  ; @usage
  ; (check-documents [{:my-namespace/my-key "My value" ...}]
  ;                  {:check-ns? true})
  ; =>
  ; [{:my-namespace/my-key "My value" ...}]
  ;
  ; @return (vector)
  ([documents]
   (check-documents documents {}))

  ([documents {:keys [e] :as options}]
   (letfn [(f0 [%] (check-document % options))]
          (cond (-> documents vector/not-empty?) (-> documents (vector/->items f0))
                (-> e some?)                     (-> e (ex-info {:documents documents}) throw)
                :return documents))))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn check-projection
  ; @description
  ; - Returns the given projection if provided as a map.
  ; - Optionally throws an error if the given projection is an empty map (if ':check-empty?' is TRUE).
  ; - Optionally throws an error if the given projection is not namespaced (if ':check-ns?' is TRUE).
  ; - Optionally throws an error if the given projection is not a map (if ':check-type?' is TRUE).
  ;
  ; @param (*) projection
  ; @param (map)(opt) options
  ; {:check-empty? (boolean)(opt)
  ;  :check-ns? (boolean)(opt)
  ;  :check-type? (boolean)(opt)}
  ;
  ; @usage
  ; (check-projection {:my-namespace/my-key -1 ...}
  ;                   {:check-ns? true})
  ; =>
  ; {:my-namespace/my-key -1 ...}
  ;
  ; @return (*)
  ([projection]
   (check-projection projection {}))

  ([projection {:keys [check-empty? check-ns? check-type?]}]
   (cond (-> projection map/empty?          (and check-empty?)) (-> messages/PROJECTION-EMPTY-ERROR           (ex-info {:projection projection}) throw)
         (-> projection map/namespaced? not (and check-ns?))    (-> messages/NAMESPACED-PROJECTION-TYPE-ERROR (ex-info {:projection projection}) throw)
         (-> projection map? not            (and check-type?))  (-> messages/PROJECTION-TYPE-ERROR            (ex-info {:projection projection}) throw)
         (-> projection map?)                                   (-> projection))))

(defn check-projections
  ; @description
  ; - Applies the 'check-projection' function on the given projections.
  ; - Options (excluding ':e') are passed to the 'check-projection' function.
  ; - Optionally throws an error if no projection is provided (if ':e' is provided).
  ;
  ; @param (vector) projections
  ; @param (map)(opt) options
  ; {:check-empty? (boolean)(opt)
  ;  :check-ns? (boolean)(opt)
  ;  :check-type? (boolean)(opt)
  ;  :e (string)(opt)}
  ;
  ; @usage
  ; (check-projections [{:my-namespace/my-key -1 ...}]
  ;                    {:check-ns? true})
  ; =>
  ; [{:my-namespace/my-key -1 ...}]
  ;
  ; @return (vector)
  ([projections]
   (check-projections projections {}))

  ([projections {:keys [e] :as options}]
   (letfn [(f0 [%] (check-projection % options))]
          (cond (-> projections vector/not-empty?) (-> projections (vector/->items f0))
                (-> e some?)                       (-> e (ex-info {:projections projections}) throw)
                :return projections))))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn check-query
  ; @description
  ; - Returns the given query if provided as a map.
  ; - Optionally throws an error if the given query is an empty map (if ':check-empty?' is TRUE).
  ; - Optionally throws an error if the given query is not namespaced (if ':check-ns?' is TRUE).
  ; - Optionally throws an error if the given query is not a map (if ':check-type?' is TRUE).
  ;
  ; @param (*) query
  ; @param (map)(opt) options
  ; {:check-empty? (boolean)(opt)
  ;  :check-ns? (boolean)(opt)
  ;  :check-type? (boolean)(opt)}
  ;
  ; @usage
  ; (check-query {:my-namespace/my-key "My value"
  ;               :$or [{:my-namespace/my-key "My value"}] ...}
  ;              {:check-ns? true})
  ; =>
  ; {:my-namespace/my-key "My value"
  ;  :$or [{:my-namespace/my-key "My value"}] ...}
  ;
  ; @return (*)
  ([query]
   (check-query query {}))

  ([query {:keys [check-empty? check-ns? check-type?]}]
   (cond (-> query map/empty?          (and check-empty?)) (-> messages/QUERY-EMPTY-ERROR           (ex-info {:query query}) throw)
         (-> query map/namespaced? not (and check-ns?))    (-> messages/NAMESPACED-QUERY-TYPE-ERROR (ex-info {:query query}) throw)
         (-> query map? not            (and check-type?))  (-> messages/QUERY-TYPE-ERROR            (ex-info {:query query}) throw)
         (-> query map?)                                   (-> query))))

(defn check-queries
  ; @description
  ; - Applies the 'check-query' function on the given queries.
  ; - Options (excluding ':e') are passed to the 'check-query' function.
  ; - Optionally throws an error if no query is provided (if ':e' is provided).
  ;
  ; @param (vector) queries
  ; @param (map)(opt) options
  ; {:check-empty? (boolean)(opt)
  ;  :check-ns? (boolean)(opt)
  ;  :check-type? (boolean)(opt)
  ;  :e (string)(opt)}
  ;
  ; @usage
  ; (check-queries [{:my-namespace/my-key "My value"
  ;                  :$or [{:my-namespace/my-key "My value"}] ...}]
  ;                {:check-ns? true})
  ; =>
  ; [{:my-namespace/my-key "My value"
  ;   :$or [{:my-namespace/my-key "My value"}] ...}]
  ;
  ; @return (vector)
  ([queries]
   (check-queries queries {}))

  ([queries {:keys [e] :as options}]
   (letfn [(f0 [%] (check-query % options))]
          (cond (-> queries vector/not-empty?) (-> queries (vector/->items f0))
                (-> e some?)                   (-> e (ex-info {:queries queries}) throw)
                :return queries))))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn check-pipeline-stage
  ; @description
  ; - Returns the given pipeline stage if provided as a map.
  ; - Optionally throws an error if the given pipeline stage is an empty map (if ':check-empty?' is TRUE).
  ; - Optionally throws an error if the given pipeline stage is not namespaced (if ':check-ns?' is TRUE).
  ; - Optionally throws an error if the given pipeline stage is not a map (if ':check-type?' is TRUE).
  ;
  ; @param (*) pipeline-stage
  ; @param (map)(opt) options
  ; {:check-empty? (boolean)(opt)
  ;  :check-ns? (boolean)(opt)
  ;  :check-type? (boolean)(opt)}
  ;
  ; @usage
  ; (check-pipeline-stage {:$unset [:my-namespace/my-key]}
  ;                       {:check-ns? true})
  ; =>
  ; {:$unset [:my-namespace/my-key]}
  ;
  ; @return (*)
  ([pipeline-stage]
   (check-pipeline-stage pipeline-stage {}))

  ([pipeline-stage {:keys [check-empty? check-ns? check-type?]}]
   (cond (-> pipeline-stage map/empty?          (and check-empty?)) (-> messages/PIPILINE-STAGE-EMPTY-ERROR           (ex-info {:pipeline-stage pipeline-stage}) throw)
         (-> pipeline-stage map/namespaced? not (and check-ns?))    (-> messages/NAMESPACED-PIPILINE-STAGE-TYPE-ERROR (ex-info {:pipeline-stage pipeline-stage}) throw)
         (-> pipeline-stage map? not            (and check-type?))  (-> messages/PIPILINE-STAGE-TYPE-ERROR            (ex-info {:pipeline-stage pipeline-stage}) throw)
         (-> pipeline-stage map?)                                   (-> pipeline-stage))))

(defn check-pipeline-stages
  ; @description
  ; - Applies the 'check-pipeline-stage' function on the given pipeline stages.
  ; - Options (excluding ':e') are passed to the 'check-pipeline-stage' function.
  ; - Optionally throws an error if no pipeline stage is provided (if ':e' is provided).
  ;
  ; @param (vector) pipeline-stages
  ; @param (map)(opt) options
  ; {:check-empty? (boolean)(opt)
  ;  :check-ns? (boolean)(opt)
  ;  :check-type? (boolean)(opt)
  ;  :e (string)(opt)}
  ;
  ; @usage
  ; (check-pipeline-stages [{:$unset [:my-namespace/my-key]}
  ;                         {:$match {:_id "MyObjectId"}}]
  ;                        {:check-ns? true})
  ; =>
  ; [{:$unset [:my-namespace/my-key]}
  ;  {:$match {:_id "MyObjectId"}}]
  ;
  ; @return (vector)
  ([pipeline-stages]
   (check-pipeline-stages pipeline-stages {}))

  ([pipeline-stages {:keys [e] :as options}]
   (letfn [(f0 [%] (check-pipeline-stage % options))]
          (cond (-> pipeline-stages vector/not-empty?) (-> pipeline-stages (vector/->items f0))
                (-> e some?)                           (-> e (ex-info {:pipeline-stages pipeline-stages}) throw)
                :return pipeline-stages))))

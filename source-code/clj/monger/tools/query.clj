
(ns monger.tools.query
    (:require [fruits.map.api     :as map]
              [monger.tools.check :as check]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn apply-dot-notation
  ; @links
  ; https://www.mongodb.com/docs/manual/core/document/#dot-notation
  ;
  ; @description
  ; - Collapses nested keys into a flat map structure within the given query.
  ; - Returns a new map where nested keys are represented using dot notation.
  ;
  ; @param (map) query
  ;
  ; @usage
  ; (apply-dot-notation {:user {:id "MyObjectId"}})
  ; =>
  ; {:user.id "MyObjectId"}
  ;
  ; @usage
  ; (apply-dot-notation {:user {:id "MyObjectId"}
  ;                      :$or [{:user {:id "MyObjectId"}}]})
  ; =>
  ; {:user.id "MyObjectId"
  ;  :$or [{:user.id "MyObjectId"}]}
  ;
  ; @return (map)
  [query]
  ; The 'except-f' function provides exception rule for the 'map/collapse' function
  ; in order to avoid collapsing operator keys in the given map.
  (letfn [(except-f [k _] (check/operator? k))]
         (map/collapse query {:keywordize? true :inner-except-f except-f :outer-except-f except-f :separator "."})))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn query<-namespace
  ; @note
  ; Multi-namespaced keywords could be a problem in future versions of Clojure!
  ; https://clojuredocs.org/clojure.core/keyword
  ;
  ; @description
  ; - Applies the given namespace to every key within the given query excluding keys that are operators.
  ; - It supports optional recursive application of the namespace to nested maps when the 'recur?' option is set to TRUE.
  ; - Using dot notation could lead to accidentally creating multi-namespaced keywords.
  ;   Therefore, this function applies the given namespace by simply prepending it to keys without altering their structure.
  ;
  ; @param (map) query
  ; @param (keyword) namespace
  ; @param (map)(opt) options
  ; {:recur? (boolean)(opt)
  ;   Default: false}
  ;
  ; @usage
  ; (query<-namespace {:id "MyObjectId" :my-keyword :my-value :$or [{:id "AnotherObjectId"}]}
  ;                   :my-namespace)
  ; =>
  ; {:my-namespace/id         "MyObjectId"
  ;  :my-namespace/my-keyword :my-value
  ;  :$or                     [{:id "AnotherObjectId"}]}
  ;
  ; @usage
  ; (query<-namespace {:id "MyObjectId" :my-keyword :my-value :my-map {:id "AnotherObjectId"}}
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
   (letfn [(f0 [k] (if (->   k check/operator?)
                       (->   k)
                       (as-> k % (str  %)
                                 (subs % 1)
                                 (str (name namespace) "/" %))))]
          (if recur? (map/->>keys query f0)
                     (map/->keys  query f0)))))

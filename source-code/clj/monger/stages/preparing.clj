
(ns monger.stages.preparing)

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn prepare-document
  ; @description
  ; - Applies the given 'prepare-f' function (if any) on the given document.
  ; - Catches and re-throws errors, extending the error message with the provided state of the document.
  ;
  ; @param (map) document
  ; @param (map) options
  ; {:prepare-f (function)(opt)}
  ;
  ; @return (map)
  [document {:keys [prepare-f]}]
  (try (if prepare-f (-> document prepare-f)
                     (-> document))
       (catch Exception e (throw (ex-info e {:document document})))))

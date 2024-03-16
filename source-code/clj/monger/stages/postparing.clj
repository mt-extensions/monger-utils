
(ns monger.stages.postparing)

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn postpare-document
  ; @description
  ; - Applies the given 'postpare-f' function (if any) on the given document.
  ; - Catches and re-throws errors, extending the error message with the provided state of the document.
  ;
  ; @param (map) document
  ; @param (map) options
  ; {:postpare-f (function)(opt)}
  ;
  ; @return (map)
  [document {:keys [postpare-f]}]
  (try (if postpare-f (-> document postpare-f)
                      (-> document))
       (catch Exception e (throw (ex-info e {:document document})))))

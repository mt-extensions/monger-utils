
(ns mongo-db.sample
    (:require [candy.api    :refer [return]]
              [mongo-db.api :as mongo-db]
              [time.api     :as time]))

;; -- Prototípusok használata -------------------------------------------------
;; ----------------------------------------------------------------------------

(defn insert-prototype
  [document]
  (merge document
         {:namespace/added-at    (time/timestamp-string)
          :namespace/modified-at (time/timestamp-string)}))

(defn update-prototype
  [document]
  (merge {:namespace/added-at    (time/timestamp-string)}
         document
         {:namespace/modified-at (time/timestamp-string)}))

;; -- Dokumentum hozzáadása (inserting) ------------------------------------------
;; ----------------------------------------------------------------------------

; - Ha NEM létezik a megadott azonosítóval rendelkező dokumentum a kollekcióban, akkor létrehozza.
; - Ha létezik a megadott azonosítóval rendelkező dokumentum a kollekcióban, akkor NEM végez műveletet!
; - A függvény visszatérési értéke sikeres mentés esetén a dokumentum.
; - Ha nem adsz meg :namespace/id tulajdonságot a dokumentumnak, akkor a függvény generál
;   számára egy string típusú azonosítót.
; - Az {:ordered? true} tulajdonság beállításával az újonnan hozzáadott dokumentum
;   {:namespace/order ...} tulajdonsága a dokumentum kollekción belül sorszámát (utolsó)
;   kapja meg értékként.
(defn insert-my-document!
  []
  (mongo-db/insert-document! "my_collection" {:namespace/my-keyword  :my-value
                                              :namespace/your-string "your-value"
                                              :namespace/id          "MyObjectId"}
                                             {:prepare-f #(insert-prototype %)}))

;; -- Dokumentum mentése (saving aka upserting by id) -------------------------
;; ----------------------------------------------------------------------------

; - Ha NEM létezik a megadott azonosítóval rendelkező dokumentum a kollekcióban, akkor létrehozza.
; - Ha létezik a megadott azonosítóval rendelkező dokumentum a kollekcióban, akkor felülírja.
; - A függvény visszatérési értéke sikeres mentés esetén a dokumentum.
; - Ha nem adsz meg :namespace/id tulajdonságot a dokumentumnak, akkor a függvény generál
;   számára egy string típusú azonosítót.
; - Az {:ordered? true} tulajdonság beállításával az újonnan hozzáadott dokumentum
;   {:namespace/order ...} tulajdonsága a dokumentum kollekción belül sorszámát (utolsó)
;   kapja meg értékként.
(defn save-my-document!
  []
  (mongo-db/save-document! "my_collection" {:namespace/my-keyword  :my-value
                                            :namespace/your-string "your-value"
                                            :namespace/id          "MyObjectId"}
                                           {:prepare-f #(update-prototype %)}))

;; -- Dokumentum felülírása (updating) ----------------------------------------
;; ----------------------------------------------------------------------------

; - Ha NEM létezik a megadott feltételeknek megfelelő dokumentum a kollekcióban, akkor NEM hozza létre!
; - Ha létezik a megadott feltételeknek megfelelő dokumentum a kollekcióban, akkor felülírja.
; - A függvény visszatérési értéke sikeres írás esetén true, minden más esetben false.
; - A feltételek tartalmazhat :namespace/id tulajdonságot!
; - A dokumentum NEM tartalmazhat :namespace/id tulajdonságot!
(defn update-my-document!
  []
  (mongo-db/update-document! "my_collection" {:namespace/id          "MyObjectId"}
                                             {:namespace/my-keyword  :my-value
                                              :namespace/your-string "your-value"}
                                             {:prepare-f #(update-prototype %)}))

;; -- Dokumentum felülírása vagy hozzáadása (upserting) -----------------------
;; ----------------------------------------------------------------------------

; - Ha NEM létezik a megadott feltételeknek megfelelő dokumentum a kollekcióban, akkor létrehozza!
; - Ha létezik a megadott feltételeknek megfelelő dokumentum a kollekcióban, akkor felülírja.
; - A függvény visszatérési értéke sikeres írás esetén true, minden más esetben false.
; - A feltételek tartalmazhat :namespace/id tulajdonságot!
; - A dokumentum NEM tartalmazhat :namespace/id tulajdonságot!
(defn upsert-my-document!
  []
  (mongo-db/upsert-document! "my_collection" {:namespace/id          "MyObjectId"}
                                             {:namespace/my-keyword  :my-value
                                              :namespace/your-string "your-value"}
                                             {:prepare-f #(update-prototype %)}))

;; -- Függvény alkalmazása dokumentumon (applying) ----------------------------
;; ----------------------------------------------------------------------------

; - Ha NEM létezik a megadott azonosítóval rendelkező dokumentum a kollekcióban, akkor NEM hozza létre!
; - Ha létezik a megadott azonosítóval rendelkező dokumentum a kollekcióban, akkor végrehajtja rajta a függvényt.
; - A függvény visszatérési értéke sikeres írás esetén a módosított dokumentum.
(defn apply-my-document!
  []
  (mongo-db/apply-document! "my_collection" "MyObjectId" #(merge % {:namespace/my-keyword :my-value})
                                            {:prepare-f  #(update-prototype %)
                                             :postpare-f #(return           %)}))

;; -- Függvény alkalmazása az összes dokumentumon (applying) ------------------
;; ----------------------------------------------------------------------------

; A függvény visszatérési értéke sikeres írás esetén a módosított dokumentumok.
(defn apply-my-documents!
  []
  (mongo-db/apply-documents! "my_collection" #(merge % {:namespace/my-keyword :my-value})
                                             {:prepare-f #(update-prototype %)}))

;; -- Dokumentum eltávolítása (removing) --------------------------------------
;; ----------------------------------------------------------------------------

; - A függvény visszatérési értéke sikeres törlés esetén a dokumentum azonosítója.
(defn remove-my-document!
  []
  (mongo-db/remove-document! "my_collection" "MyObjectId"))

;; -- Dokumentum duplikálása (duplicating) ------------------------------------
;; ----------------------------------------------------------------------------

; - A függvény visszatérési értéke sikeres duplikálás esetén a másolat dokumentum.
(defn duplicate-my-document!
  []
  (mongo-db/duplicate-document! "my_collection" "MyObjectId" {:prepare-f #(insert-prototype %)}))

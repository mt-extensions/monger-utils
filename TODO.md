
# temp

# mongo-db.actions

- {create? true} beállítás, azokhoz a függvényekhez, amelyek ignorálják a műveletet,
  nem létező dokumentum esetén.

- Az apply-*, remove-*, duplicate-* függvények query-t fogadjanak, mert az használható
  id alapú azonosításhoz is!

# mongo-db.adaptation

- A mongo-db.adaptation névtér függvényei az egyes térképeken többszörösen iterálnak végig,
  hogy elvégezzék az adaptálást. Ezen az eljáráson ha szükséges, lehetséges gyorsítani.
  Elegendő függvényenként egy iteráció, ami elvégzi a kulcsokon és értékeiken a műveleteket.

# mongo-db.pipelines

- A pipelines névtér xyz-query függvényei nem rekurzívan járják be az átadott pattern-t.
  Pl. a filter-query függvénynek átadott filter-pattern adatot rekurzívan kellene bejárni,
      hogy átadható legyen akár egy ilyen minta is:
  Pl. {:$or  [{...} {...}]
       :$and [{:$or [{...} {...}]}]}
  A rekurzív bejárás során a problémát az jelenti, hogy egy vektorban felsorolt kulcsszavak,
  json/unkeywordize-value függvénnyel való adaptálása * biztonsági prefixummal látja el
  a string típusra alakított kulcsszavakat. Viszont a pattern-ekben nem csak a dokumentumokból
  származó adatok, hanem a mongo-db utasításai is vannak, és az utasításokban nem kellene
  a biztosági prefixumot használni:
  Pl. {:namespace/name {:$concat [:$namespace/first-name " " :$namespace/last-name]}

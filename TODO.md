
# mongo-db.actions

- Az apply-document! függvényből hiányzik, hogy szükség esetén létrehozza a nem létező
  dokumentumot és, hogy akár query alapján is lehessen apply-olni dokumentumokat,
  ne csak id alapján!
  =>
  Legjobb lenne, ha a függvényekből vagy létezni *-by-id és *-by-query változat,
  vagy a paraméter megvizsgálná, hogy string vagy map típust kapott-e és az alapján
  kezelné id vagy query-ként!
  +
  Legjobb lenne, ha a függvények fogadnának egy olyan beállítást, hogy {:create? true},
  ami alapján létrehozná/nem hozná létre a nem létező dokumentumot!

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

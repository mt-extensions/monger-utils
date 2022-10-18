
# mongo-db.actions
- A get-document-copy-label függvény nincs használatban!
  Ha szükséges a dokumentumról készülő másolat címkéjét megjelölni másolat-jelzővel,
  akkor talán célszerűbb a "My document copy 1" kifejezés helyett a "My document #1" jelölést alkalmazni!



# mongo-db.adaptation
- A mongo-db.adaptation névtér függvényei az egyes térképeken többszörösen iterálnak végig,
  hogy elvégezzék az adaptálást. Ezen az eljáráson ha szükséges, lehetséges gyorsítani.
  Elegendő függvényenként egy iteráció, ami elvégzi a kulcsokon és értékeiken a műveleteket.



# mongo-db.pipelines
- A pipelines névtér xyz-query függvényei nem rekurzívan járják be az átadott pattern-t.
  Pl a filter-query függvénynek átadott filter-pattern adatot rekurzívan kellene bejárni, hogy
  átadható legyen akár egy ilyen minta is:
  pl. {:$or  [{...} {...}]
       :$and [{:$or [{...} {...}]}]}
  A rekurzív bejárás során a problémát az jelenti, hogy egy vektorban felsorolt kulcsszavak,
  json/unkeywordize-value függvénnyel való adaptálása * biztonsági prefixummal látja el
  a string típusra alakított kulcsszavakat. Viszont a pattern-ekben nem csak a dokumentumokból
  származó adatok, hanem a mongo-db utasításai is vannak, és az utasításokban nem kellene
  a biztosági prefixumot használni:
  pl. {:namespace/name {:$concat [:$namespace/first-name " " :$namespace/last-name]}

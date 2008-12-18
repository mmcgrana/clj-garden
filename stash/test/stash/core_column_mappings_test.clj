(in-ns 'stash.core-test)

(defmacro defquote-parse-test
  [type quote-map cast-map]
  `(let [quoter# (column-quoter ~type)
         caster# (column-caster ~type)]
     (deftest (str ~type " quoting")
       (doseq [[val# expected-quote#] ~quote-map]
         (assert= expected-quote# (quoter# val#))))
     (deftest (str ~type " casting")
       (doseq [[val# expected-cast#] ~cast-map]
          (assert= expected-cast# (caster# val#))))))

(defquote-parse-test :boolean
  {true "'t'" false "'f'"}
  {"t" true "f" false})

(defquote-parse-test :integer
  {0 "0"  3 "3" 765 "765" -1 "-1" -23 "-23"}
  {"0" 0 "3" 3 "765" 765 "-1" -1 "-23" -23})

(defquote-parse-test :float
  {0.0  "0.0" 7.65 "7.65" -1.2 "-1.2"}
  {"0.0" (Float. 0.0) "7.65" (Float. 7.65) "-1.2" (Float. -1.2)})

(defquote-parse-test :string
  {"foo bar"   "'foo bar'"
   "`_-+;"     "'`_-+;'"
   "foo'bar"   "'foo''bar'"
   "foo\\bar"  "'foo\\\\bar'"}
  {"foo bar"   "foo bar"
   "`_-+;"     "`_-+;"
   "foo'bar"   "foo'bar"
   "foo\\bar"  "foo\\bar"
   "foo''\\\\" "foo''\\\\"})

(defquote-parse-test :uuid
  {"a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11"
     "'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11'"}
  {"a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11"
    "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11"})

(def- the-time
  (org.joda.time.DateTime.
    2008 12 18 1 55 17 797
    (org.joda.time.DateTimeZone/UTC)))

(defquote-parse-test :datetime
  {the-time "'2008-12-18T01:55:17.797Z'"}
  {"2008-12-18 01:55:17.797" the-time})
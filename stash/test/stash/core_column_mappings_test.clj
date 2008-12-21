(in-ns 'stash.core-test)

; also test nil
(def quote-cast-test-cases
  {:uuid     ["a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11"]
   :boolean  [true false]
   :integer  [0 3 765 -1 -23]
   :long     [0 12345678987654321]
   :float    [(float 0.0) (float 1.23) (float -1.23)]
   :double   [0.0, 0.00000000000001, 1.2345678987654321]
   :string   ["foo bar" "foo'bar''biz\\bat\\\\bang"]
   :datetime [(now)]})

(doseq [[type values] quote-cast-test-cases]
  (doseq [val values]
    (deftest-db (str "??: " type " quote and cast round trips" val)
      (let [schmor (assoc empty-schmorg type val)]
        (persist-insert schmor)
        (assert=
          (get schmor type)
          (get (find-one +schmorg+) type))))))

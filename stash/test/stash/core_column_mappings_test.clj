(in-ns 'stash.core-test)

(def a-datetime (now))

; also test nil
(def quote-cast-test-cases
  {:uuid     [nil "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11"]
   :boolean  [nil true false]
   :integer  [nil 0 3 765 -1 -23]
   :long     [nil 0 12345678987654321]
   :float    [nil (float 0.0) (float 1.23) (float -1.23)]
   :double   [nil 0.0, 0.00000000000001, 1.2345678987654321]
   :string   [nil "foo bar" "foo'bar''biz\\bat\\\\bang"]
   :datetime [nil a-datetime]})

(def parse-test-cases
  {:uuid     {nil nil
              "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11"
                "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11"}
   :boolean  {nil nil true true false false
              "t" true "1" true "true" true
              "f" false "0" false "false" false}
   :integer  {nil nil (int 3) (int 3) (long 3) (int 3)
              (float 3.3) (int 3) (double 3.7) (int 3)
              "3.3" (int 3) "foo" nil}
   :long     {nil nil (int 3) (long 3) (long 3) (long 3)
              (float 3.3) (long 3) (double 3.7) (long 3)
              "3.3" (long 3) "foo" nil}
  :float     {nil nil (int 3) (float 3) (long 3) (float 3)
              (float 3.3) (float 3.3) (double 3.7) (float 3.7)
              "3.3" (float 3.3) "foo" nil}
  :double    {nil nil (int 3) (double 3) (long 3) (double 3)
              (double 3.7) (double 3.7)
              "3.3" (double 3.3) "foo" nil}
  :string    {nil nil "foo" "foo" 3.3 "3.3"}
  :datetime  {nil nil a-datetime a-datetime}})

(doseq [[type values] quote-cast-test-cases]
  (let [cname (keyword (str "a_" (name type)))]
    (doseq [val values]
      (deftest-db (str type " quote and parse round trips: " val)
        (let [schmor (assoc +simple-schmorg+ cname val)]
          (persist-insert schmor)
          (assert=
            (get schmor cname)
            (get (find-one +schmorg+) cname)))))))

(doseq [[type cases] parse-test-cases]
  (doseq [[unparsed parsed] cases]
    (deftest (str type " casting " unparsed " to " parsed)
      (assert= parsed ((type-caster type) unparsed)))))

(deftest "type-db-type"
  (assert= "varchar" (type-db-type :string)))

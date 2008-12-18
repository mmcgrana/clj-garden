(def- column-mappers
  {:boolean
    {:quoter #(if % "'t'" "'f'")
     :caster #(if (= % "t") true false)}
   :integer
    {:quoter str
     :caster #(Integer. %)}
   :float
    {:quoter str
     :caster #(Float. %)}
   :string
    {:quoter #(str "'" (re-gsub #"'" "''" (re-gsub #"\\" "\\\\\\\\" %)) "'")
     :caster identity}
   :uuid
    {:quoter #(str "'" % "'")
     :caster identity}
   :datetime
    {:quoter #(str "'" % "'")
     :caster #(org.joda.time.DateTime.
                (re-sub #"\s" "T" %)
                (org.joda.time.DateTimeZone/UTC))}})

(defn column-quoter
  "Returns the quoter fn corresponding to the type."
  [type]
  (get-in column-mappers [type :quoter]))

(defn column-caster
  "Returns the caster fn corresponding to the type."
  [type]
  (get-in column-mappers [type :caster]))

(def +uuid-re+
  #"^[0-9a-z]{8}-[0-9a-z]{4}-[0-9a-z]{4}-[0-9a-z]{4}-[0-9a-z]{12}$")

(def- non-nil-column-mappers
  {:uuid
    {:primitive "uuid"
     :quoter    #(str "'" % "'")
     :parser    (fn [#^org.postgresql.util.PGobject obj] (.getValue obj))
     :caster    #(let [s (str %)] (if (re-matches? +uuid-re+ s) s))}
   :boolean
    {:primitive "bool"
     :quoter    #(if % "'t'" "'f'")
     :parser    identity
     :caster    #(let [s (str %)] (or (= "true" s) (= "1" s) (= "t" s)))}
   :integer
    {:primitive "int4"
     :quoter    str
     :parser    identity
     :caster    #(try (new Integer %) (catch Exception e nil))}
   :long
    {:primitive "int8"
     :quoter    str
     :parser    identity
     :caster    #(try (new Long %) (catch Exception e nil))}
   :float
    {:primitive "float4"
     :quoter    str
     :parser    identity
     :caster    #(try (new Float %) (catch Exception e nil))}
   :double
    {:primitive "float8"
     :quoter    str
     :parser    identity
     :caster    #(try (new Double %) (catch Exception e nil))}
   :string
    {:primitive "varchar"
     :quoter    #(str "'" (re-gsub #"'" "''" (re-gsub #"\\" "\\\\\\\\" %)) "'")
     :parser    identity
     :caster    str}
   :datetime
    {:primitive "timestamp"
     :quoter    (fn [dt] (str "'" dt "'"))
     :parser    (fn [#^java.sql.Timestamp ts]
                  (org.joda.time.DateTime.
                    (+ 1900 (.getYear ts))
                    (+ 1 (.getMonth ts))
                    (.getDate ts)
                    (.getHours ts)
                    (.getMinutes ts)
                    (.getSeconds ts)
                    (/ (.getNanos ts) 1000000)
                    (org.joda.time.DateTimeZone/UTC)))
     :caster    #(throwf "not implemented")}})

(def- column-mappers
  (reduce
    (fn [mappers [type {:keys [quoter parser caster]}]]
      (assoc mappers type
        {:quoter #(if (nil? %) "NULL" (quoter %))
         :parser #(if (nil? %) nil    (parser %))
         :caster #(if (nil? %) nil    (caster %))}))
    {}
    non-nil-column-mappers))

(defn type-quoter
  "Returns the quoter fn corresponding to the type."
  [type]
  (get-in column-mappers [type :quoter]))

(defn type-parser
  "Returns the parser fn corresponding to the type."
  [type]
  (get-in column-mappers [type :parser]))

(defn type-caster
  "Returns the caster fn corresponding to the the type"
  [type]
  (get-in column-mappers [type :caster]))
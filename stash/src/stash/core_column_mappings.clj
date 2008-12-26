(def +uuid-re+
  #"^[0-9a-z]{8}-[0-9a-z]{4}-[0-9a-z]{4}-[0-9a-z]{4}-[0-9a-z]{12}$")

(def- undecorated-column-mappers
  {:uuid
    {:clj-type  String
     :db-type   "uuid"
     :quoter    #(str "'" % "'")
     :parser    (fn [#^org.postgresql.util.PGobject obj] (.getValue obj))
     :caster    identity}
   :boolean
    {:clj-type  Boolean
     :db-type   "bool"
     :quoter    #(if % "'t'" "'f'")
     :parser    identity
     :caster    #(let [s (str %)] (or (= "true" s) (= "1" s) (= "t" s)))}
   :integer
    {:clj-type  Integer
     :db-type   "int4"
     :quoter    str
     :parser    identity
     :caster    #(cond
                   (number? %) (.intValue %)
                   (string? %) (try (.intValue (new Float %)) (catch Exception e nil)))}
   :long
    {:clj-type  Long
     :db-type   "int8"
     :quoter    str
     :parser    identity
     :caster    #(cond
                    (number? %) (.longValue %)
                    (string? %) (try (.longValue (new Double %)) (catch Exception e nil)))}
   :float
    {:clj-type  Float
     :db-type   "float4"
     :quoter    str
     :parser    identity
     :caster    #(cond
                    (number? %) (.floatValue %)
                    (string? %) (try (new Float %) (catch Exception e nil)))}
   :double
    {:clj-type  Double
     :db-type   "float8"
     :quoter    str
     :parser    identity
     :caster    #(cond
                    (number? %) (.doubleValue %)
                    (string? %) (try (new Double %) (catch Exception e nil)))}
   :string
    {:clj-type  String
     :db-type   "varchar"
     :quoter    #(str "'" (re-gsub #"'" "''" (re-gsub #"\\" "\\\\\\\\" %)) "'")
     :parser    identity
     :caster    str}
   :datetime
    {:clj-type  org.joda.time.DateTime
     :db-type   "timestamp"
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
     ;TODO: implement
     :caster    identity}})

(def- column-mappers
  (mash
    (fn [[type {:keys [clj-type db-type quoter parser caster]}]]
      [type
       {:quoter #(if (nil? %) "NULL" (quoter %))
        :parser #(if (nil? %) nil    (parser %))
        :caster #(if-not (nil? %)
                   (if (instance? clj-type %) % (caster %)))}])
    undecorated-column-mappers))

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
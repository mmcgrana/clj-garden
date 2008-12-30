(def +uuid-re+
  #"^[0-9a-z]{8}-[0-9a-z]{4}-[0-9a-z]{4}-[0-9a-z]{4}-[0-9a-z]{12}$")

; clj-type:
;   in-memory type of values in attrs hash. values must always either of this
;   type or nil.
; db-type:
;   postgres column type.
; quoter:
;   returns a String that can be used in sql to represent an instance of
;   clj-type.
; paresr:
;   returns an instance of clj-type based on the value provided by the postgres
;   driver's getObject.
; caster:
;   returns the an instance of clj-type corresponding to the input, which can
;   be of any type, or nil if such typecasting is not possible.

(def- undecorated-column-mappers
  {:uuid
    {:clj-type  String
     :db-type   "uuid"
     :quoter    #(str "'" % "'")
     :parser    (fn [#^org.postgresql.util.PGobject obj] (.getValue obj))
     :caster    #(let [s (str %)] (if (re-match? +uuid-re+ s) s))}
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
     :caster    identity}})

(def- column-mappers
  (mash
    (fn [[type {:keys [clj-type db-type quoter parser caster]}]]
      [type
       {:db-type db-type
        :quoter  #(if (nil? %) "NULL" (quoter %))
        :parser  #(if (nil? %) nil    (parser %))
        :caster  #(if-not (nil? %)
                    (if (instance? clj-type %) % (caster %)))}])
    undecorated-column-mappers))

(defn type-db-type
  "Returns a String for the Postgres column type corresponding to the type."
  [type]
  (get-in column-mappers [type :db-type]))

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
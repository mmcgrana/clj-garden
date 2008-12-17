(def- columns
  {:int
    {:quoter
       (fn [int] (str int))
     :caster
       (fn [int-str] (Integer. int-str))}
   :string
    {:quoter
       (fn [string]
         (str "'" (re-gsub #"'" "''" (re-gsub #"\\" "\\\\" string)) "'"))
     :caster
       identity}})


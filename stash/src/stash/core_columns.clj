(def- column-mappers
  {:boolean
    {:quoter
      (fn [boolean] (if boolean "t" "f"))
     :caster
      (fn [string] (if (= string "t") true false))}
   :integer
    {:quoter
       (fn [int] (str int))
     :caster
       (fn [string] (Integer. string))}
   :float
    {:quoter
      (fn [float] (str float))
     :caster
      (fn [string] (Float. string))}
   :string
    {:quoter
       (fn [string]
         (str "'" (re-gsub #"'" "''" (re-gsub #"\\" "\\\\" string)) "'"))
     :caster
       identity}})
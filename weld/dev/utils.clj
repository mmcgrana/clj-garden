(defn re-before
  "Returns the portion of the given string occuring before the first match of
  the given pattern, or the whole string if there is no match".
  [pattern string]
  (first (re-split pattern string)))

(defn str-blank?
  "Returns true iff the given string is nil or has 0 length."
  [#^String string]
  (or (nil? string) (= (.length string) 0)))



(defn plus [x]
  (println "adding")
  (+ x x))

(def plus (memoize plus)

(def plus
  (memoize
    (fn [x] + x x)))

; Our function in question
(defn expensive-slurp [x]
  (println "call me only once")
  (str "cache x"))

; With delay
(let [slurped (delay (expensive-slurp))]
  [(force slurped)
   (force slurped)])

; prints "call me only once" & returns ["cache this" "cache this"], as desired

; But what if coordination via the shared 'slurped' variable is impractical, i.e.
; the two calls are very apart on the call graph and we don't want to pass the 
; delay around?

; mpp -> params*



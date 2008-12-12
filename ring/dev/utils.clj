(defn re-before
  "Returns the portion of the given string occuring before the first match of
  the given pattern, or the whole string if there is no match".
  [pattern string]
  (first (re-split pattern string)))

(defn re-get
  "Returns the nth captured group resulting from matching the given pattern
  against the given string, or nil if no match is found."
  [re s n]
  (let [m (re-matcher re s)]
    (if (.find m)
      (.group m n))))

(defn str-blank?
  "Returns true iff the given string is nil or has 0 length."
  [#^String string]
  (or (nil? string) (= (.length string) 0)))
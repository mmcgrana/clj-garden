(ns clj-time.core)

(def +utc-zone+ (org.joda.time.DateTimeZone/UTC))

(defn now
  "Returns a joda DateTime instance for the current time in the UTC zone."
  []
  (org.joda.time.DateTime. (org.joda.time.DateTimeZone/UTC)))
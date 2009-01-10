(ns clj-time.core
  (:import
    (org.joda.time.format ISODateTimeFormat)
    (org.joda.time DateTime DateTimeZone)))

(defn now
  "Returns a joda DateTime instance for the current time in the UTC zone."
  []
  (DateTime. (DateTimeZone/UTC)))

(defn zero
  "Returns a joda DateTime instance corresponding to the begining of the epoch."
  []
  (DateTime. (long 0) (DateTimeZone/UTC)))

(def #^{:private true} iso-formatter (ISODateTimeFormat/dateTime))

(defn xmlschema
  "Returns a String representation of the given datetie suitable for use 
  in xml documents."
  [dt]
  (.print iso-formatter dt))

;(time/xmlschema (time/now))
;DateTime dt = new DateTime();
;DateTimeFormatter fmt = ISODateTimeFormat.dateTime();
;String str = fmt.print(dt);
(in-ns 'clj-unit.core)

(defn assert-truth
  "Encapsulates the common pattern of reporting success if some value is
  logically true or reporting failure with a message otherwise."
  [condition message]
  (if condition (success) (failure message)))

(defn flunk
  "Always fail, optionally with the given message"
  [& [message]]
  (failure (or message "flunk")))

(defn assert=
  "Assert that two values are equal according to =."
  [expected actual]
  (assert-truth (= expected actual)
    (format "Expected %s, got %s" (pr-str expected) (pr-str actual))))

(defn assert-not=
  "Assert that two values are not equal according to =."
  [expected actual]
  (assert-truth (not (= expected actual))
    (format "Expected value other than %s" (pr-str actual))))

(defn assert-in-delta
  "Assert that a value is +-delta of another value"
  [expected actual delta]
  (assert-truth (and (> (- expected delta) actual)
                    (< actual (+ expected delta)))
    (format "Expected %s +-%s, got %s" expected delta actual)))

(defn assert-that
  "Assert that a value is logically true - i.e. not nil or false."
  [val]
  (assert-truth val (format "Expected logical truth, got %s" val)))

(defn assert-not
  "Assert that a value is logically false - i.e. either nil or false."
  [val]
  (assert-truth (not val)
    (format "Expected logical false, got %s" val)))

(defn assert-nil
  "Assert that a value is nil."
  [val]
  (assert-truth (nil? val) (format "Expected nil, got %s" val)))

(defn assert-fn
  "Assert that a function returns logical truth when given a val."
  [pred val]
  (assert-truth (pred val)
    (format "Expected pred to return logical truth for %s, but it did not."
      val)))

(defn assert-not-fn
  "Assert that a function returns logical false when given a val."
  [pred val]
  (assert-truth (not (pred val))
    (format "Expected pred to return logical false for %s, but it did not."
      val)))

(defn assert-instance
  "Assert that an object is an instance of a class according to instance?"
  [expected-class actual-instance]
  (assert-truth (instance? expected-class actual-instance)
    (format "Expected an instance of %s, but %s is not."
      expected-class actual-instance)))

(defn assert-isa
  "Assert that an object is a child of a parent according to isa?"
  [expected-parent actual-child]
  (assert-truth (isa? actual-child expected-parent)
    (format "Expected a child of %s, but %s is not."
      expected-parent actual-child)))

(defn assert-match
  "Asserts that a String matches a pattern"
  [expected-pattern actual-string]
     (assert-truth (re-find expected-pattern actual-string)
       (format "Expected a string matching %s, but %s does not"
         (pr-str expected-pattern) (pr-str actual-string))))

(defmacro assert-throws
  "Assert that a form throws. Note that unlike all other assertions, which are
  functions, assert-throws is macro."
  ([form]            (assert-throws Exception nil        form))
  ([message-re form] (assert-throws Exception message-re form))
  ([klass message-re form]
   `(try
      ~form
      (failure "Expecting throw, got none")
      (catch ~klass e#
        (let [e-message# (.getMessage e#)]
          (assert-truth (or (not ~message-re) (re-match? ~message-re e-message#))
            (format "Expected message matching \"%s\" got \"%s\""
              ~message-re e-message#))))
      (catch Exception e#
        (failure (format "Expected class %s got %s" ~klass (class e#)))))))
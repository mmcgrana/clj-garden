(in-ns 'clj-unit.core)

(defmacro assert-that
  "Encapsulates the common pattern of reporting success if some value is
  logically true or reporting failure with a message otherwise."
  [condition-form message-form]
  `(if ~condition-form (success) (failure ~message-form)))

(defmacro flunk
  "Always fail, optionally with the given message"
  [& [message]]
    `(failure (or ~message "flunk")))

(defmacro assert=
  "Assert that two values are equal according to =."
  [expected-form actual-form]
  `(let [expected# ~expected-form
         actual#   ~actual-form]
     (assert-that (= expected# actual#)
       (format "Expected %s, got %s" (pr-str expected#) (pr-str actual#)))))

(defmacro assert-in-delta
  "Assert that a value is +-delta of another value"
  [expected-form delta-form actual-form]
  `(let [expected# ~expected-form
         delta#    ~delta-form
         actual#   ~actual-form]
    (assert-that (and (> (- expected# delta#) actual#)
                      (< actual# (+ expected# delta#)))
      (format "Expected %s +-%s, got %s" expected# delta# actual#))))

(defmacro assert-truth
  "Assert that a value is logically true - i.e. not nil or false."
  [form]
  `(let [val# ~form]
     (assert-that val#
       (format "Expected logical truth, got %s" val#))))

(defmacro assert-not
  "Assert that a value is logically false - i.e. either nil or false."
  [form]
  `(let [val# ~form]
     (assert-that (not val#)
       (format "Expected logical false, got %s" val#))))

(defmacro assert-nil
  "Assert that a value is nil."
  [form]
  `(let [val# ~form]
     (assert-that (nil? val#)
       (format "Expected nil, got %s" val#))))

(defmacro assert-fn
  "Assert that a function returns logical truth when given a val."
  [pred-form val-form]
  `(let [val# ~val-form]
     (assert-that val#
       (format "Expected pred to return logical truth for %s, but it did not."
         val#))))

(defmacro assert-not-fn
  "Assert that a function returns logical false when given a val."
  [pred-form val-form]
  `(let [val# ~val-form]
     (assert-that (not val#)
       (format "Expected pred to return logical false for %s, but it did not."
         val#))))

(defmacro assert-instance
  "Assert that an object is an instance of a class according to instance?"
  [expected-class-form actual-instance-form]
  `(let [expected# ~expected-class-form
         actual#   ~actual-instance-form]
     (assert-that (instance? expected# actual#)
       (format "Expected an instance of %s, but %s is not."
         expected# actual#))))

(defmacro assert-isa
  "Assert that an object is a child of a parent according to isa?"
  [expected-parent-form actual-child-form]
  `(let [expected# ~expected-parent-form
         actual#   ~actual-child-form]
     (assert-that (isa? actual# expected#)
       (format "Expected a child of %s, but %s is not." expected# actual#))))

(defmacro assert-match
  "Asserts that a String matches a pattern"
  [expected-pattern-form actual-string-form]
  `(let [expected# ~expected-pattern-form
         actual#   ~actual-string-form]
     (assert-that (re-find expected# actual#)
       (format "Expected a string matching %s, but %s does not"
         (pr-str expected#) (pr-str actual#)))))

(defmacro assert-throws
  "Assert that a form throws."
  ([form]            (assert-throws Exception nil     form))
  ([message-re form] (assert-throws Exception message-re form))
  ([klass message-re form]
   `(try
      ~form
      (failure "Expecting throw, got none")
      (catch ~klass e#
        (if (test-failure-exception? e#)
          (throw e#)
          (let [e-message# (.getMessage e#)]
            (assert-that (or (not ~message-re) (re-match? ~message-re e-message#))
              (format "Expected message matching \"%s\" got \"%s\""
                ~message-re e-message#)))))
      (catch Exception e#
        (if (test-failure-exception? e#)
         (throw e#)
         (failure (format "Expected class %s got %s" ~klass (class e#))))))))
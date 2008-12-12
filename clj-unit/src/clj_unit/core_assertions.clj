(in-ns 'clj-unit.core)

(defn success
  "Report a successfull assertion."
  []
  ((*reporter* :success)))

(defn failure
  "Report a failed assertion, with a message indicating the reason."
  [message]
  ((*reporter* :failure) *test-doc* message))

(defmacro assert-that
  "Encapsulates the common pattern of reporting success if some value is
  logically true or reporting failure with a message otherwise."
  [condition-form message-form]
  `(if ~condition-form (success) (failure ~message-form)))

(defmacro flunk
  "Always fail, optionally with the given message"
  [& [message]]
    (failure (or message "flunk")))

(defmacro assert=
  "Assert that two values are equal according to =."
  [expected-form actual-form]
  `(let [expected# ~expected-form
         actual#   ~actual-form]
     (assert-that (= expected# actual#)
       (format "Expected %s, got %s" expected# actual#))))

(defmacro assert-in-delta
  "Assert that a value is +-delta of another value"
  [expected-form delta-form actual-form]
  `(let [expected# ~expected-form
         delta#    ~delta-form
         actual#   ~actual-form]
    (assert-that (and (> (- expected# delta#) actual#)
                      (< actual# (+ expected# delta#)))
      (format "Expected %s +-%s, got %s" expected# delta# actual#))))

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

(defmacro assert-isa
  "Assert that an object is a child of a parent according to isa?"
  [expected-parent-form actual-child-form]
  `(let [expected# ~expected-parent-form
         actual#   ~actual-child-form]
     (assert-that (isa? actual# expected#)
       (format "Expected a child of %s, but %s is not." expected# actual#))))

(defmacro assert-throws
  "Assert that a form throws."
  ([form]         (assert-throws Exception nil     form))
  ([message form] (assert-throws Exception message form))
  ([klass message form]
    `(try
       ~form
       (failure "Expecting throw, got none")
       (catch ~klass e#
         (let [e-message# (.getMessage e#)]
           (assert-that (or (not ~message) (= ~message e-message#))
             (format "Expected message \"%s\" got \"%s\""
               ~message e-message#))))
       (catch Exception e#
         (failure (format "Expected class %s got %s" ~klass (class e#)))))))
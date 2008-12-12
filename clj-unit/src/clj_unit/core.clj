; deftest
; assert=
; assert-is
; assert-not
; assert-nil
; pending

; test-ns

; print results

; assert [1 2 3] (comp + 1) [2 3 4]

; return value based or not???
; perhaps if can return an assertion or a seq-able of assertions

; possibilities
; * compile-time error
; * exception before assertion
; * exception in inner assertion forms
; * returns non-assertion
; * returns a failing assertion
; * returns a passing assertion

(ns clj-unit.core)

(defstruct test-info :doc :test-fn)

(defstruct assertion :result :reason)

(def tests-info (ref {}))

(defmacro deftest
  "Define a unit test."
  [doc & body]
  (let [test-fn (if (empty? body) nil `(fn [] ~@body))]
    `(let [ns-sym# (.name *ns*)
           info#   (struct test-info ~doc ~test-fn)]
      (dosync
        (alter tests-info
          assoc ns-sym# (conj (@tests-info ns-sym# []) info#))))))

(defmacro assert=
  "Assert that two values are equal according to =."
  [expected-form actual-form]
  `(let [expected# ~expected-form
         actual#   ~actual-form]
     (if (= expected# actual#)
       (struct assertion true)
       (struct assertion false
         (format "Expected %s, got %s" expected# actual#)))))

(defmacro assert-not
  "Assert that a value is logically false - i.e. either nil or false."
  [form]
  `(let [val# ~form]
     (if (not val#)
       (struct assertion true)
       (struct assertion false
         (format "Expected logical false, got %s" val#)))))

(defmacro assert-nil
  "Assert that a value is nil."
  [form]
  `(let [val# ~form]
     (if (nil? val#)
       (struct assertion true)
       (struct assertion false
         (format "Expected nil, got %s" val#)))))

(defmacro assert-isa
  "Assert that an object is a child of a parent according to isa?"
  [expected-parent-form actual-child-form]
  `(let [expected# ~expected-parent-form
         actual#   ~actual-child-form]
     (if (isa? actual# expected#)
       (struct assertion true)
       (struct assertion false
         (format "Expected a child of %s, but %s is not." expected# actual#)))))

(defmacro assert-throws
  "Assert that a form throws."
  ([form] (assert-throws Exception nil form))
  ([message form] (assert-throws Exception message form))
  ([klass message form]
    `(try
       ~form
       (struct assertion false "Expecting throw, got none")
       (catch ~klass e#
         (let [e-message# (.getMessage e#)]
           (if (or (not ~message) (= ~message e-message#))
             (struct assertion true)
             (struct assertion false
               (format "Expected message \"%s\" got \"%s\""
                 ~message e-message#)))))
       (catch Exception e#
         (struct assertion false
           (format "Expected class %s got %s" ~klass (class e#)))))))

(defn run-tests
  "Run all tests for the namespace identified by the given sym."
  [ns-sym]
  (if-let [ns-tests-info (@tests-info ns-sym)]
    (do
      (printf "Testing: %s\n" ns-sym)
      (doseq [{:keys [doc test-fn]} ns-tests-info]
        (if test-fn
          (try
            (let [returned   (test-fn)
                  assertions (if (seq? returned) returned (list returned))
                  failed     (remove #(:result %) assertions)]
                (if (empty? failed)
                  (printf "PASS: %s\n" doc)
                  (printf "FAIL: %s (%s)\n" doc (:reason (first failed)))))
            (catch Exception e
              (printf "EXCP: %s\n" doc)
              (.printStackTrace e)))
          (printf "PEND: %s\n" doc))))
    (printf "No tests for: %s\n" ns-sym)))
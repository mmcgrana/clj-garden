(ns clj-backtrace.core-test
  (:use clj-unit.core (clj-backtrace core utils)))

(def cases-data
  [["foo.bar$biz__123" "invoke" "bar.clj" 456
    {:clojure true :ns "foo.bar" :fn "biz" :file "bar.clj" :line 456 :annon-fn false}]

   ["foo.bar$biz__123$fn__456" "invoke" "bar.clj" 789
    {:clojure true :ns "foo.bar" :fn "biz" :file "bar.clj" :line 789 :annon-fn true}]

   ["clojure.lang.Var" "invoke" "Var.java" 123
    {:java true :class "clojure.lang.Var" :method "invoke" :file "Var.java" :line 123}]

   ["clojure.proxy.space.SomeClass" "someMethod" "SomeClass.java" 123
    {:java true :class "clojure.proxy.space.SomeClass" :method "someMethod" :file "SomeClass.java" :line 123}]

   ["some.space.SomeClass" "someMethod" "SomeClass.java" 123
    {:java true :class "some.space.SomeClass" :method "someMethod" :file "SomeClass.java" :line 123}]

   ["some.space.SomeClass$SomeInner" "someMethod" "SomeClass.java" 123
    {:java true :class "some.space.SomeClass$SomeInner" :method "someMethod" :file "SomeClass.java" :line 123}]

   ["some.space.SomeClass" "someMethod" nil -1
    {:java true :class "some.space.SomeClass" :method "someMethod" :file nil :line nil}]])

(def cases
  (mash
    (fn [[c m f l p]] [(StackTraceElement. c m f l) p])
    cases-data))

(deftest "parse-elem"
  (doseq [[elem parsed] cases]
    (assert= parsed (parse-elem elem))))

(deftest "parse-trace"
  (assert= (map second cases) (parse-trace (map first cases))))


  1:28 user=> (.getClassName native-elem)
  "sun.reflect.NativeMethodAccessorImpl"
  1:29 user=> (.getFileName native-elem)
  "NativeMethodAccessorImpl.java"
  1:30 user=> (.getLineNumber native-elem)
  -2
  1:31 user=> (.getMethodName native-elem)
  "invoke0"
  1:32 user=> (.isNativeMethod native-elem)
  true


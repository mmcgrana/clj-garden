(ns clj-routing.core-test
  (:use clj-unit.core
        clj-routing.core))

(def routes
  [[:index-bars  :get "foo/bars"                         ]
   [:show-bar    :get "foo/bars/:id"                     ]
   [:create-bat  :put "foo/bars"                         ]
   [:index-bars2 :get "foo/bars"                         ]
   [:by-any      :any "foo/biz/:id"                      ]
   [:with-conds  :get "foo/:action" {:action "biz|bats"} ]
   [:with-root   :get ":root/foo"                        ]
   [:catch-all   :get "foo/:path"   {:path   ".*"}       ]])

(def gen (compile-generator routes))
(def rec (compile-recognizer routes))


(deftest "Returns static route with all params unused"
  (assert= [:get "foo/bars" {:extra "stuff"}]
    (gen :index-bars {:extra "stuff"}))
  (assert= [:get "foo/bars" {:extra "stuff"}]
    (gen :index-bars {:extra "stuff"}))
  (assert= [:get "foo/bars" {:extra "stuff"}]
    (gen :index-bars {:extra "stuff"})))

(deftest "Returns static route when no params given"
  (assert= [:get "foo/bars" nil]
    (gen :index-bars)))

(deftest "Returns static route with all params unused"
  (assert= [:get "foo/bars" {:extra "stuff"}]
    (gen :index-bars {:extra "stuff"})))

(deftest "Returns dynamic routes with some params unused"
  (assert= [:get "foo/bars/27" {:extra "stuff"}]
    (gen :show-bar {:id "27" :extra "stuff"})))

(deftest "Returns dynamic routes with all params used"
  (assert= [:get "foo/bars/27" {}]
    (gen :show-bar {:id "27"})))

(deftest "Works with leading params"
  (assert= [:get "bar/foo" {}]
    (gen :with-root {:root "bar"})))

(deftest "Uses get for any routes"
  (assert= [:get "foo/biz/13" {}]
    (gen :by-any {:id "13"})))

(deftest "Coerces non-string param values"
  (assert= [:get "foo/bars/27" {}]
    (gen :show-bar {:id 27})))

(deftest "Throws if missing param"
  (assert-throws #"Missing param: :id"
    (gen :show-bar {})))


(deftest "Throws if no routes match"
  (assert-throws (rec :get "biz/bat")))

(deftest "Returns empty params for a static route"
  (assert= [:index-bars {}] (rec :get "foo/bars")))

(deftest "Returns dynamic params for a dynamic route"
  (assert= [:show-bar {:id "45"}] (rec :get "foo/bars/45")))

(deftest "Matches routes only from the given methed"
  (assert= [:create-bat {}]
    (rec :put "foo/bars")))

(deftest "Matches routes with conditions when they are met"
  (assert= [:with-conds {:action "bats"}]
    (rec :get "foo/bats")))

(deftest "Doesn't match routes with conditions when not met"
  (assert= [:catch-all {:path "bees"}]
    (rec :get "foo/bees")))

(deftest "Can match any method to an :any route"
  (assert= [:by-any {:id "13"}]
    (rec :post "foo/biz/13")))

(deftest "Works with leading param"
  (assert= [:with-root {:root "bar"}]
    (rec :get "bar/foo")))

(deftest "Amenable to catch-all routes"
  (assert= [:catch-all {:path "bar/bat.biz"}]
    (rec :get "foo/bar/bat.biz")))

(deftest "Enforces start of path"
  (assert= [:catch-all {:path "foo/bars"}]
    (rec :get "foo/foo/bars")))

(deftest "Enforces end of path"
  (assert= [:catch-all {:path "bars/some/more"}]
    (rec :get "foo/bars/some/more")));)

(in-ns 'stash.core-test)

(defn the-extension
  [model-map]
  (update-in model-map [:callbacks :after-validation]
    (nf [av-callbacks] (conj (or av-callbacks [:the-callback])))))

(deftest "compiled-model: extensions"
  (let [extended-model
          (compiled-model (assoc +post-map+ :extensions [the-extension]))]
    (assert= [:the-callback] (:after-validation (:callbacks extended-model)))))
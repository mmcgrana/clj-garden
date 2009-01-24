(ns cling.view-helpers
  (:use
    (clojure.contrib prxml))
  (:import
    (org.eclipse.mylyn.wikitext.core.parser MarkupParser)
    (org.eclipse.mylyn.wikitext.textile.core TextileLanguage)))

(defn textilize [markup]
  (let [parser      (MarkupParser. (TextileLanguage.))
        html        (.parseToHtml parser markup)
        post-header (.substring html 169)
        body        (.substring post-header 0 (- (.length post-header) 14))]
    body))

(defmacro xml
  [& body]
  `(with-out-str (prxml ~@body)))

(defmacro defxml
  [name args & body]
  `(defn ~name ~args (xml ~@body)))
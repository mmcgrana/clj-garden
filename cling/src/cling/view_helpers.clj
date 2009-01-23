(ns cling.view-helpers
  (:import
    (org.eclipse.mylyn.wikitext.core.parser MarkupParser)
    (org.eclipse.mylyn.wikitext.textile.core TextileLanguage)))

(defn textilize [markup]
  (let [parser      (MarkupParser. (TextileLanguage.))
        html        (.parseToHtml parser markup)
        post-header (.substring html 169)
        body        (.substring post-header 0 (- (.length post-header) 14))]
    body))


foo

@@:clj
(some clojure code)
@@

foobar @@:clj (+ 1 2)@@ biz bat

========

foo

CODE1

foobar CODE2 biz bat

========

<p>foo</p><p>CODE1</p><p>foobar CODE2 biz bat</p>

========

<p>foo</p><pre class="code-block clj">
(<span class="keyword">some</span> clojure code)
</pre><p>foobar <pre class="code-inline clj">(+ 1 2)</pre> biz bat</p>
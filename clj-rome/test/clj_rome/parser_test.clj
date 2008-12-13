(ns clj-rome.parser-test
  (:use clj-unit.core
        clj-rome.parser)
  (:import (java.io ByteArrayInputStream)))

(def +rss-example-str+
"<?xml version=\"1.0\"?>
<rss version=\"2.0\">
  <channel>
    <title>Example Channel</title>
    <link>http://example.com/</link>
    <description>My example channel</description>
    <item>
       <title>News for September the Second</title>
       <link>http://example.com/2002/09/01</link>
       <description>other things happened today</description>
    </item>
    <item>
       <title>News for September the First</title>
       <link>http://example.com/2002/09/02</link>
    </item>
  </channel>
</rss>")

(def +rss-example-output+
  {:channel
    {:title "Example Channel",
     :description "My example channel",
     :link "http://example.com/"
     :pub-date nil},
   :entries (list
     {:title "News for September the Second",
      :description "other things happened today",
      :link "http://example.com/2002/09/01",
      :pub-date nil,
      :guid "http://example.com/2002/09/01"}
     {:title "News for September the First",
      :description nil,
      :link "http://example.com/2002/09/02",
      :pub-date nil,
      :guid "http://example.com/2002/09/02"})})

(def +atom-example-str+
"<?xml version=\"1.0\" encoding=\"utf-8\"?>
<feed xmlns=\"http://www.w3.org/2005/Atom\">
  <title>Example Feed</title>
  <link href=\"http://example.org/\"/>
  <updated>2003-12-13T18:30:02Z</updated>
  <author>
    <name>John Doe</name>
  </author>
  <id>urn:uuid:60a76c80-d399-11d9-b93C-0003939e0af6</id>

  <entry>
    <title>Atom-Powered Robots Run Amok</title>
    <link href=\"http://example.org/2003/12/13/atom03\"/>
    <id>urn:uuid:1225c695-cfb8-4ebb-aaaa-80da344efa6a</id>
    <updated>2003-12-13T18:30:02Z</updated>
    <summary>Some text.</summary>
  </entry>
</feed>")

(def +atom-example-output+
  {:channel
    {:title "Example Feed",
     :description nil,
     :link "http://example.org/",
     :pub-date (java.util.Date. (long 1071340202000))},
   :entries (list
    {:title "Atom-Powered Robots Run Amok",
     :description "Some text.",
     :link "http://example.org/2003/12/13/atom03",
     :pub-date nil,
     :guid "urn:uuid:1225c695-cfb8-4ebb-aaaa-80da344efa6a"})})

(deftest "rss parsing works"
  (assert=
    +rss-example-output+
    (parse-feed (ByteArrayInputStream. (.getBytes +rss-example-str+)))))

(deftest "atom parsing works"
  (assert=
    +atom-example-output+
    (parse-feed (ByteArrayInputStream. (.getBytes +atom-example-str+)))))

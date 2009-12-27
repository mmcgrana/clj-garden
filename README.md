clj-garden
==========

Various Clojure libraries and apps I'm working on:
 
 * `cling:`: A Clojure wiki, built with Weld
 * `clj-bin`: Helper files that I use when working with Clojure at the terminal
 * `clj-cache`: A caching abstraction for clojure
 * `clj-file-utils`: Unix-like wrapper for Commons FileUtils
 * `clj-jdbc`: High-level interface to JDBC databases'
 * `clj-rome`: Universal RSS/Atom Feed parser wrapping Rome
 * `clj-scrape`: Bundling of various clojure libraries to facilitate HTML scraping
 * `clj-time`: Wrapper around Joda Time library
 * `stash`: Functional ORM
 * `stash-migrations`: Migration for Stash models
 * `stash-pagination`: Paginate Stash find results 
 * `stash-timestamps`: Timestamping callbacks for Stash models
 * `weld-blog-example`: Example of a blog in Weld
 * `weld-upload-example`: Example of file uploading in Weld

I've "harvested" several projects from `clj-garden`, moving them to their own top-level projects:

 * [`clj-stacktrace`](http://github.com/mmcgrana/clj-stacktrace): Tools for readable backtraces in Clojure programs
 * [`clj-routing`](http://github.com/mmcgrana/clj-routing) Core of URL router for use in web frameworks
 * [`clj-unit`](http://github.com/mmcgrana/clj-unit) Unit testing library: non-magical, developer-friendly
 * [`gitcred`](http://github.com/mmcgrana/gitcred) PageRanking GitHub users
 * [`ring`](http://github.com/mmcgrana/ring) Web application library: abstracts HTTP to allow modular and concise webapps
 * [`weld`](http://github.com/mmcgrana/weld) Web framework built on Ring: expressive and fast in a functional style

License
-------

Copyright 2009 Mark McGranaghan and released under an MIT license.

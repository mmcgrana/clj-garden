GitCred attempts to measure the 'cred' of GitHub users. It works by fetching followers data from GitHub pages and then applying the classic PageRank algorithm to the resulting users/followers graph.

You can find the results for a recent GitCred computation in `RESULTS.txt`. These values are logarithmically scaled and normalized so that they correspond to the familiar Google PageRank measure.

Implementation Notes
--------------------

GitCred itself is implemented in Clojure. It leverages the Jung graph library to perform the actual PageRank computations and the FleetDB database to manage and persist the graph state.

Reproducing the Results
-----------------------

To run the GitCred calculations yourself, you will need the following dependencies in your classpath:

* `clojure.jar` & `clojure-contrib.jar`: recent versions from the masters at `http://github.com/richhickey`
* `clj-stacktrace.jar`: , `http://github.com/mmcgrana/clj-stacktrace`
* `fleetdb.jar`: `http://cloud.github.com/downloads/mmcgrana/fleetdb/fleetdb.jar`
* `jung-*.jar` and their associated dependencies: `http://sourceforge.net/projects/jung/files/`
* `src/` in this distribution

Then to start populating the user/followers graph database, run:

    $ clj bin/fetch.clj /path/to/data.fdb

If the data file does not exist it will be created for you. If it does exist the database will be loaded from it and fetching will continue from there.

Once you have some data in the database, you can generate results with:

    $ clj bin/write.clj /path/to/data.fdb /path/to/results.txt


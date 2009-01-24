(ns stash.pagination
  (:use stash.core))

(defn page
  "Returns the current page of the pager."
  [p]
  (get p :page))

(defn per-page
  "Returns the number of entires per page in the pager."
  [p]
  (get p :per-page))

(defn total-entries
  "Returns the total number of entires in the paged results for the pager."
  [p]
  (get p :total-entries))

(defn entries
  "Returns the entries on this page for the pager."
  [p]
  (get p :entries))

(defn total-pages
  "Returns the total number of pages in the paged results for the pager."
  [p]
  (let [te (total-entries p)
        pp (per-page p)]
    (if (zero? (rem te pp)) (/ te pp) (+ (quot te pp) 1))))

(defn out-of-bounds?
  "Returns true if the page is out of bounds - on either side - of the paged
  resuls for the pager."
  [p]
  (or (< (page p) 1) (> (page p) (total-pages p))))

(defn offset
  "Returns the 0-indexed offset of the first entry in this page among the
  paged results for the pager."
  [p]
  (* (- (page p) 1) (per-page p)))

(defn next-page
  "Returns the number of the next page, if there is a next page."
  [p]
  (if (< (page p) (total-pages p)) (+ (page p) 1)))

(defn previous-page
  "Returns the number of the previous page, if there is a previous page."
  [p]
  (if (> (page p) 1) (- (page p) 1)))

(defn pager
  "Construct a pager object.
  Both page and per-page are required and must be integers. count-fn is a fn
  of arity 1 that takes a pager object and returns the total number of entires
  in the collection. find-fn likewise takes a pager and returns the indicated
  page of entries. 
  The returned object can then be used as arguments to e.g. next-page."
  [page per-page count-fn find-fn]
  (let [pgr {:page page :per-page per-page}
        pgr (assoc pgr :total-entries (count-fn pgr))]
    (if (out-of-bounds? pgr)
      pgr
      (assoc pgr :entries (find-fn pgr)))))

(defn paginate
  "Returns a pager object for a paginated search on the model.
  Options are :page, :per_page, :order, and :where."
  [model & [options]]
  (let [page     (or (get options :page) 1)
        per-page (or (get options :per-page) 10)
        where    (get options :where)
        order    (get options :order)
        limit    (get options :limt)]
    (pager page per-page
      (fn [p] (count-all model {:where where}))
      (fn [p] (find-all  model {:where where :order order
                                :limit per-page :offset (offset p)})))))

(ns stash.pagination
  (:use stash.core))


; options: :page :per_page :order :where 
(defn paginate
  "Returns a [results paginator] tuple for a paginated search on the model.
  Options are :page, :per_page, :order, and :where."
  [model & [options]]
  (let [page     (get options :page 1)
        per-page (get options :per-page 10)]
        where    (get options :where)
        order    (get options :order)
        limit    (get options :limt)
    (pager/init page per-page
      (fn [offset]
        [(count-all model
           {:where where})
         (find-all  model
           {:where where :order order :limit per-page :offset offset})]))))


(defn init
  [page per-page]
  )
    (paginator/new page per-page)))
  (paginator/new (:page options) (:per-page options)
    (fn [p] [(count-all model {:where (get options :where)})
             (orm/all model {:where  (:where options)
                             :order  (:order options)
                             :limit  (:per-page options)
                             :offset (paginator/offset p)})])))

(paginate +post+ {:page     (params request :page)
                  :per-page 15
                  :order    [:created_at :desc]})

(let [paginated-posts {:current-page  2
                       :per-page      15
                       :total-entries 57
                       :entries       [:foo :bar :bat]}])
(pagination-links )

(paginator 2 15 (fn [p] () ))

(paginator/next-page paginator)
(paginator/offset    paginator)
(defn find-article
  [request]
  (stash/find-one +article+ {:where [:id = (params request :id)]}))

(defmacro with-article
  [[article-sym request-form] & body]
  (if-let [~article-sym (stash/find-article ~request-form)]
    ~@body
    (not-found (v/not-found))))

; def index
;   @articles = Article.all
;   display @articles
; end

(defn index
  [request]
  (render (v/index (stash/find-all +article+))))

; def show(id)
;   @article = Article.get(id)
;   raise NotFound unless @article
;   display @article
; end

(defn show
  [request]
  (with-article [article request]
    (render (v/show article))))

; def new
;   only_provides :html
;   @article = Article.new
;   display @article
; end

(defn new
  [request]
  (render (v/new (stash/init +article+))))

; def edit(id)
;   only_provides :html
;   @article = Article.get(id)
;   raise NotFound unless @article
;   display @article
; end

(defn edit
  [request]
  (with-article [article request]
    (render (v/edit article))))

; def create(article)
;   @article = Article.new(article)
;   if @article.save
;     redirect resource(@article), :message => {:notice => "Article was successfully created"}
;   else
;     message[:error] = "Article failed to be created"
;     render :new
;   end
; end

(defn create
  [request]
  (let [article (stash/create +article+ (params request :article))]
    (if (stash/valid? article)
      (with-flash :article_success
        (redirect (path :article article)))
        (render (v/new article)))))

; def update(id, article)
;   @article = Article.get(id)
;   raise NotFound unless @article
;   if @article.update_attributes(article)
;      redirect resource(@article)
;   else
;     display @article, :edit
;   end
; end

(defn update
  [request]
  (with-article [article request]
    (let [article (stash/update article (params request :article))]
      (if (stash/valid? article)
        (redirect (path :article article))
        (render (v/edit article))))))

; def destroy(id)
;   @article = Article.get(id)
;   raise NotFound unless @article
;   if @article.destroy
;     redirect resource(:articles)
;   else
;     raise InternalServerError
;   end
; end

(defn destroy
  [request]
  (with-article [article request]
    (destroy article)
    (redirect (path :articles))))
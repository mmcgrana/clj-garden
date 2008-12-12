(ns clj-akismetor.api
  (:use clj-http-client.core))

; after the rails pugin.
;
; key blog user_ip user_agent referrer permalink comment_type
; comment_author comment_email comment_author_url comment_content
(defn spam?
  [attrs]
  (not= (execute "comment-check" attrs) "false"))

(defn submit-spam
  "Submit a page as spam."
  [attrs]
  (execute "submit-spam" attrs))

(defn submit-ham
  "Submit a page as ham."
  [attrs]
  (execute "submit-ham" attrs))

(defvar- +http-headers+
  {"User-Agent"   "Akismetor Clojure Library/0.0"})

(defvar- +form-ct+ "application/x-www-form-urlencoded")

(defn- execute
  [command attrs]
  (let [url        (str (:key attrs) ".rest.akismet.com/1.1/" command)
        payload    (serialize attrs)
        [_ _ body] (http-post url +http-headers+ payload +form-ct+)]
    body))
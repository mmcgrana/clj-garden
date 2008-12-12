General
  App is arity-1 function
  Returns status, headers, body
  Probably explicit wrapper around servlets

Env Interface
  (not neccessary)
  :http-servlet-request
    hsr

  :content-type
    hsr.getContentType() => "text/html"
  :content-length
    hsr.getContentLength() => 10203, -1
  :uri
    hsr.getRequestURI() => "/some/path.html"
  :query-string
    hsr.getQueryString()=> "foo=bar"
  :scheme
    hsr.getScheme() => "https"

  :request-method
    hsr.getMethod() => "POST"

  :server-port
    hsr.getServerPort() => 80
  :server-name
    hsr.getServerName() => "www.google.com" "123.223.222"

  :remote-addr
    hsr.getRemoteAddr() => "12.3234.32"

  :headers
    hsr.getHeaderNames() => "name"
    hsr.getHeader(name) => "foobar"

  :reader-fn
    (memefn getReader)
  :stream-fn
    (memfn getInputStream)


Response ReverseInterface
  App provides
  Servlet Needs
    setStatus(int)
    forEach: setHeader(String, String)
    setBody
    Nothing else unless we really need it.


inspector wrapper - renders the env as nice html for debugging / inpsection

Specifying handlers

sews together apps and handlers


body - String, File, or InputStream eventually
Strings for now
note flush() with output stream
(ns my.namespace
  (:use cwsgi.core cwsgi.handler.jetty))

(defn app
  [evn]
  [200 {} "Hello Worold"])

(cwsgi.core/run cwsgi.handler.jetty/handle app {:port 4444})

java marshaling, clojure marshaling



think creatively about inverting control to deal wiht stubbing - 
eg pass in functions that do "IO" work, but in tests just pass in appropriate
closures.
streaming / piping to output stream
asynchronous

allow if-let and when-let to take multiple forms

#!/bin/sh

cd `dirname $0`/..

cp=$cp:/Users/mmcgrana/Desktop/remote/clojure/clojure.jar
cp=$cp:/Users/mmcgrana/Desktop/remote/clojure-contrib/clojure-contrib.jar
cp=$cp:/Users/mmcgrana/Desktop/remote/compojure/jars/servlet-api-2.5-6.1.11.jar 
cp=$cp:/Users/mmcgrana/Desktop/remote/compojure/jars/jetty-6.1.11.jar
cp=$cp:/Users/mmcgrana/Desktop/remote/compojure/jars/jetty-util-6.1.11.jar
cp=$cp:/Users/mmcgrana/Desktop/raw/scratch/jetty_sample/lib/commons/commons-io-1.4.jar
cp=$cp:/Users/mmcgrana/Desktop/raw/wag/src/

java -cp $cp clojure.lang.Script bin/run.clj

(ns twitter.core
  (:use
   [clojure.test]
   [twitter.handlers]
   [twitter.oauth]
   [twitter.utils])
  (:require
   [clojure.contrib.json :as json]
   [oauth.client :as oa]
   [http.async.client :as ac]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn fix-keyword
  "Takes a parameter name and replaces the - with a _"
  [param-name]

  (keyword (.replace (name param-name) \- \_)))
  
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(deftest test-fix-keyword
  (is (= (fix-keyword :my-test) :my_test)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn make-uri 
   "makes a uri from a supplied protocol, site, version and resource-path"
   ([protocol site version resource-path]
      (str protocol "://" site "/" version "/" resource-path))
   ([protocol site resource-path]
      (str protocol "://" site "/" resource-path)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(deftest test-make-uri
  (is (= (make-uri "http" "api.twitter.com" 1 "users/show.json") "http://api.twitter.com/1/users/show.json"))
  (is (= (make-uri "http" "api.twitter.com" "users/show.json") "http://api.twitter.com/users/show.json")))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn add-form-content-type
  "adds a content type of url-encoded-form to the supplied headers"
  [headers]
  (merge headers
         {:content-type "application/x-www-form-urlencoded"}))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn sync-http-request 
  "calls the action on the resource specified in the uri, signing with oauth in the headers
   you can supply args from async.http.client (e.g. :query, :body etc) but also you can give
   :params which will transform its keys from lisp-friendly dashes to http header-friendly _'s.
   So :params could be {:screen-name 'blah'} and it be merged with :query as {:screen_name 'blah'}"
  [action uri & args]

  (let [arg-map (apply hash-map args)
        oauth-creds (or (:oauth-creds arg-map) *oauth-creds*)
        handler-fn (or (:handler-fn arg-map) (make-default-handler))

        body (:body arg-map)
        query (merge (:query arg-map)
                     (transform-map (:params arg-map) :key-trans fix-keyword))
        oauth-map (sign-query oauth-creds action uri :query query)
        headers (merge (:headers arg-map)
                       {:Authorization (oauth-header-string oauth-map)})

        [http-verb my-args] (cond (= action :get)
                                    [ac/GET (hash-map :query query :headers headers :body body)]
                                  (nil? body)
                                    [ac/POST (hash-map :headers (add-form-content-type headers) :body query)]
                                  :else
                                  [ac/POST (hash-map :query query :headers headers :body body)])
        http-args (merge (dissoc arg-map :query :headers :body :oauth-creds :handler-fn :params)
                         my-args)]

    (handler-fn (ac/await (apply http-verb uri (apply concat http-args))))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defmacro def-sync-twitter-method
  "declares a synchronous twitter method that is named from the resource path"
  [name action uri]

  `(defn ~name
     [& args#]
     
     (apply sync-http-request
            ~action
            ~uri
            args#)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

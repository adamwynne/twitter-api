(ns twitter.core
  (:use
   [clojure.test]
   [twitter.callbacks]
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

(defn subs-uri
  "substitutes parameters for tokens in the uri"
  [uri params]

  (loop [matches (re-seq #"\{\:(\w+)\}" uri)
         ^String result uri]
    (if (empty? matches) result
        (let [[token kw] (first matches)
              value (get params (keyword kw))]
          (if-not value (throw (Exception. (format "%s needs :%s param to be supplied" uri kw))))
          (recur (rest matches) (.replace result token (str value)))))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(deftest test-sub-uri
  (is (= (subs-uri "http://www.cnn.com/{:version}/{:id}/test.json" {:version 1, :id "my123"})
         "http://www.cnn.com/1/my123/test.json"))
  (is (= (subs-uri "http://www.cnn.com/nosubs.json" {:version 1, :id "my123"})
         "http://www.cnn.com/nosubs.json"))
  (is (thrown? Exception (subs-uri "http://www.cnn.com/{:version}/{:id}/test.json" {:id "my123"}))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def memo-create-client (memoize ac/create-client))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn default-client 
  "makes a default async client for the http comms"
  []
  (memo-create-client :follow-redirects true))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn http-args 
  "takes uri action and optional args and returns the http parameters for the subsequent call"
  [#^keyword action
   #^String uri
   #^PersistentArrayMap arg-map]

  (let [params (transform-map (:params arg-map) :key-trans fix-keyword)
        body (:body arg-map)
        query (merge (:query arg-map) params)
        
        final-uri (subs-uri uri params)
        oauth-map (sign-query (:oauth-creds arg-map) action final-uri :query query)
        headers (merge (:headers arg-map)
                       (if oauth-map {:Authorization (oauth-header-string oauth-map)}))

        my-args (cond (= action :get) (hash-map :query query :headers headers :body body)
                      (nil? body) (hash-map :headers (add-form-content-type headers) :body query)
                      :else (hash-map :query query :headers headers :body body))]

    [final-uri (merge (dissoc arg-map :query :headers :body :params :oauth-creds) my-args)]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn stream-http-request 
  ""
  [#^keyword action
   #^String uri
   & args]

  (let [arg-map (apply hash-map args)
        
        client (or (:client arg-map) (default-client))
        callbacks (or (:callbacks arg-map) (default-callbacks))

        [final-uri final-args] (http-args action uri (dissoc arg-map :client :on-success :on-failure))
        
        response (apply ac/stream-seq client action final-uri (apply concat final-args))
        status (ac/status response)]

    (println final-args)
    (println final-uri)
    [(ac/string response)] 
    ;(doseq [r response-seq] (return-fn r))
           ))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn sync-http-request 
  "calls the action on the resource specified in the uri, signing with oauth in the headers
   you can supply args for async.http.client (e.g. :query, :body etc) but also you can give
   :params which will transform its keys from lisp-friendly dashes to http header-friendly _'s.
   So :params could be {:screen-name 'blah'} and it be merged with :query as {:screen_name 'blah'}"
  [^keyword action
   ^String uri
   & args]

  (let [arg-map (apply hash-map args)
        
        client (or (:client arg-map) (default-client))
        callbacks (or (:callbacks arg-map) (default-callbacks))

        [final-uri final-args] (http-args action uri (dissoc arg-map :client :callbacks))
        http-verb (if (= action :get) ac/GET ac/POST)]

    (handle-response
     callbacks
     (ac/await
      (apply http-verb client final-uri (apply concat final-args))))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defmacro def-twitter-sync-method
  "declares a synchronous twitter method that is named from the resource path"
  [name action uri]

  `(defn ~name
     [& args#]
     
     (apply sync-http-request
            ~action
            ~uri
            args#)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

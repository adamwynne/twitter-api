(ns twitter.core
  (:use
   [clojure.test]
   [twitter.callbacks]
   [twitter.oauth]
   [twitter.api]
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

(defn add-form-content-type
  "adds a content type of url-encoded-form to the supplied headers"
  [headers]
  (merge headers
         {:content-type "application/x-www-form-urlencoded"}))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def memo-create-client (memoize ac/create-client))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn default-client 
  "makes a default async client for the http comms"
  []
  (memo-create-client :follow-redirects false))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn process-http-args 
  "takes uri action and optional args and returns the final uri and http parameters for the subsequent call"
  [^keyword action
   ^String uri
   ^PersistentArrayMap arg-map]

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

    [final-uri
     (merge (dissoc arg-map :query :headers :body :params :oauth-creds :client :callbacks) my-args)]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn http-request 
  "calls the action on the resource specified in the uri, signing with oauth in the headers
   you can supply args for async.http.client (e.g. :query, :body etc) but also you can give
   :params which will transform its keys from lisp-friendly dashes to http header-friendly _'s.
   So :params could be {:screen-name 'blah'} and it be merged with :query as {:screen_name 'blah'}"
  [create-response-fn
   ^keyword action
   ^String uri
   ^PersistentArrayMap arg-map]

  (let [client (or (:client arg-map) (default-client))
        callbacks (:callbacks arg-map)
        
        [final-uri final-args] (process-http-args action uri arg-map)]

    (handle-response callbacks
                     (create-response-fn client action final-uri final-args))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defmacro def-method
  "declares a twitter method that is named from the resource path, with a function to create the response, and
   also the default callbacks appropriate for the type of call"
  [name action uri create-response-fn default-callbacks & {:as rest}]

  `(defn ~name
     [& {:as args#}]
     
     (let [arg-map# (merge {:callbacks ~default-callbacks}
                           ~rest
                           args#)]

       (http-request ~create-response-fn
                     ~action
                     ~uri
                     arg-map#))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn create-sync-response
  "calls a synchronous method waits for, and then returns the response"
  [client action uri arg-map]

  (ac/await
   (apply (if (= action :get) ac/GET ac/POST)
          client
          uri
          (apply concat arg-map))))
  
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defmacro def-sync-method
  "declares a synchronous method that is named from the resource path"
  [name action uri & rest]

  `(def-method ~name ~action ~uri create-sync-response (sync-callbacks-default) ~@rest))
  
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn create-streaming-response
  "calls a streaming method and returns the response"
  [client action uri arg-map]

  (apply ac/stream-seq
         client
         action
         uri
         (apply concat arg-map)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defmacro def-streaming-method
  "declares a streaming method that is named from the resource path"
  [name action uri & rest]

  `(def-method ~name ~action ~uri create-streaming-response (streaming-callbacks-default) ~@rest))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defmacro def-twitter-method
  "forms the full uri from the supplied params and declares the method using the supplied method-macro"
  [method-macro api-context name action resource-path & rest]

  (let [uri (make-uri api-context resource-path)]
    `(~method-macro ~name ~action ~uri ~@rest)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

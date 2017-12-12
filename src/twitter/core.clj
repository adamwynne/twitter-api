(ns twitter.core
  (:require [clojure.string :as str]
            [http.async.client :as ac]
            [twitter.api :refer [make-uri subs-uri]]
            [twitter.oauth :refer [oauth-header-string sign-query]]
            [twitter.request :refer [execute-request-callbacks
                                     prepare-request-with-multi]]
            [twitter.utils :refer [transform-map]])
  (:import (clojure.lang Keyword PersistentArrayMap)))

(defn- fix-keyword
  "Takes a parameter name and replaces the - with a _"
  [param-name]
  (keyword (str/replace (name param-name) \- \_)))

(defn- fix-colls
  "Turns collections into their string, comma-sep equivalents"
  [val]
  (if (coll? val) (str/join "," val) val))

(defn- add-form-content-type
  "adds a content type of url-encoded-form to the supplied headers"
  [headers]
  (merge headers
         {:content-type "application/x-www-form-urlencoded"}))

(def memo-create-client (memoize ac/create-client))

(defn default-client
  "makes a default async client for the http comms"
  []
  (memo-create-client :follow-redirects false :request-timeout -1))

(defn- get-request-args
  "takes uri, verb and optional args and returns the final uri and http parameters for the subsequent call.
   Note that the params are transformed (from lispy -'s to x-header-style _'s) and added to the query. So :params
   could be {:screen-name 'blah'} and it be merged into :query as {:screen_name 'blah'}. The uri has the params
   substituted in (so {:id} in the uri with use the :id in the :params map). Also, the oauth headers are added
   if required."
  [verb uri arg-map]
  (let [params (transform-map (:params arg-map) :key-trans fix-keyword :val-trans fix-colls)
        body (:body arg-map)
        query (merge (:query arg-map) params)
        final-uri (subs-uri uri params)
        oauth-map (if (contains? (:oauth-creds arg-map) :bearer)
                    (:oauth-creds arg-map) ;; no need to sign for app-only auth
                    (sign-query (:oauth-creds arg-map)
                                verb
                                final-uri
                                :query query))
        headers (merge (:headers arg-map)
                       (when oauth-map {:Authorization (oauth-header-string oauth-map)}))
        my-args (cond (= verb :get) {:query query :headers headers :body body}
                      (nil? body) {:headers (add-form-content-type headers) :body query}
                      :else {:query query :headers headers :body body})]
    {:verb verb
     :uri final-uri
     :processed-args (merge (dissoc arg-map :query :headers :body :params :oauth-creds :client :api :callbacks)
                            my-args)}))

(defn http-request
  "calls the verb on the resource specified in the uri, signing with oauth in the headers
   you can supply args for async.http.client (e.g. :query, :body, :headers etc)."
  [verb uri arg-map]
  (let [client (or (:client arg-map) (default-client))
        callbacks (or (:callbacks arg-map)
                      (throw (ex-info "need to specify a callback argument for http-request"
                                      {:arg-map arg-map})))
        request-args (get-request-args verb uri arg-map)
        request (apply prepare-request-with-multi
                       (:verb request-args)
                       (:uri request-args)
                       (apply concat (:processed-args request-args)))]
    (execute-request-callbacks client request callbacks)))

(defmacro def-twitter-method
  "Declares a twitter method with the supplied name, HTTP verb and relative resource path.
   As part of the specification, it must have an :api and :callbacks member of the 'rest' list.
   From these it creates a uri, the api context and relative resource path. The default callbacks that are
   supplied, determine how to make the call (in terms of the sync/async or single/streaming)"
  {:requires [make-uri]}
  [fn-name default-verb resource-path & rest]
  (let [rest-map (apply sorted-map rest)]
    `(defn ~fn-name
       [& {:as args#}]
       (let [arg-map# (merge ~rest-map args#)
             api-context# (or (:api arg-map#)
                              (throw (ex-info "must include an ':api' entry in the params"
                                              {:arg-map arg-map#})))
             verb# (or (:verb args#) ~default-verb)
             uri# (make-uri api-context# ~resource-path)]
         (http-request verb# uri# arg-map#)))))

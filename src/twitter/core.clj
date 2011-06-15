(ns twitter-client.core
  (:use
   [clojure.test])
  (:require
   [oauth.client :as oa]
   [http.async.client :as ac]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defstruct oauth-creds
  :consumer
  :access-token
  :access-token-secret)

(def *oauth-creds* (struct oauth-creds))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn process-query-params 
  "takes oauth credentials and signs the query parameters and urlencoding them"
  [oauth-creds action url query]
  (merge query
         (oauth/credentials (:consumer oauth-creds)
                            (:access-token oauth-creds)
                            (:access-token-secret oauth-creds)
                            action
                            url
                            query)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn make-test-creds
  "creates a set of test credentials for the api tests"

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(deftest test-process-query-params
  (let [app-key "4NZ24o0FnUMT4ngO6Lg1ow"
        app-secret "8W5UTZIspWQ3HDhUSW8gnCNn5JHJJDrUbCtI3O0UY"
        consumer (oa/make-consumer app-key
                                   app-secret
                                   "https://twitter.com/oauth/request_token"
                                   "https://twitter.com/oauth/access_token"
                                   "https://twitter.com/oauth/authorize"
                                   :hmac-sha1)
        access-token
        access-token-secret]

  (process-query-params )
  
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn sync-http-request 
  "prepares the  request from the supplied parameters, headers, url and protocol"
  [action url &
   {:keys [query headers body oauth-creds return-fn]
    :or {oauth-creds *oauth-creds*,
         return-fn #({:headers (ac/headers %) :body (ac/string %)})}}]
  
  (let [oauthed-query (map (fn [k v] (oauth-sign oauth-creds action url query)))
        response (when (= action :post)
                   (
        query
        args '(:query query :headers headers :body body)
        response (apply action-fn url args)]
    (ac/await response)
    (return-fn response)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

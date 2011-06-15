(ns twitter.core
  (:use
   [clojure.test])
  (:require
   [clojure.contrib.json :as json]
   [oauth.client :as oa]
   [oauth.signature :as oas]
   [http.async.client :as ac]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defstruct oauth-creds
  :consumer
  :access-token
  :access-token-secret)

(def *oauth-creds* (struct oauth-creds))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defmacro with-oauth-creds
  "rebinds the oauth creds to the supplied ones"
  [^oauth-creds oauth-creds & body]
  
  `(binding [*oauth-creds* ~oauth-creds]
     ~@body))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn sign-query-params 
  "takes oauth credentials and signs the query parameters, merging the new oauth params into the returned map"
  [oauth-creds action uri & {:keys [query]}]
  (merge query
         (oa/credentials (:consumer oauth-creds)
                         (:access-token oauth-creds)
                         (:access-token-secret oauth-creds)
                         action
                         uri
                         query)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn make-test-creds
  "creates a set of test credentials for the api tests"

  []
  (let [app-key "4NZ24o0FnUMT4ngO6Lg1ow"
        app-secret "8W5UTZIspWQ3HDhUSW8gnCNn5JHJJDrUbCtI3O0UY"
        consumer (oa/make-consumer app-key
                                   app-secret
                                   "https://twitter.com/oauth/request_token"
                                   "https://twitter.com/oauth/access_token"
                                   "https://twitter.com/oauth/authorize"
                                   :hmac-sha1)
        access-token "15321630-qagHj675nqKYYwFe2GOVq859V5TYkfOZai8GI4OB0"
        access-token-secret "7saU2FHfHGBFPDpBWYsrxiHnVrowmZFUNizpi1RZd8M"]

    (struct oauth-creds consumer access-token access-token-secret)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(deftest test-sign-query-params
  (let [result (sign-query-params (make-test-creds) :get "http://www.cnn.com" :query {:test-param "true"})]
    (is (:oauth_signature result))))
  
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn fix-keyword
  "Takes a parameter name and replaces the - with a _"
  [param-name]

  (keyword (.replace (name param-name) \- \_)))
  
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn transform-map
  "transforms the k/v pairs of a map using a supplied transformation function"
  [m & {:keys [key-trans val-trans] :or {key-trans (fn [x] x) val-trans (fn [x] x)}}]
  
  (into {} (map (fn [[k v]] [(key-trans k) (val-trans v)]) m)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn action-2-function 
  "maps the keyword of the action to the respective http verb function"
  [kw]
  (cond (= kw :get) ac/GET
        (= kw :post) ac/POST
        (= kw :delete) ac/DELETE))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn url-encode-val 
  "takes a value and returns the url encoding of the string of it"
  [val]
  (oas/url-encode (str val)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn query-transform
  "returns a function that contains the transformation of the query params required, depending on the action"
  [action sign-fn url-encode-fn fix-keys-fn]

  (cond (= action :get) #(sign-fn (url-encode-fn (fix-keys-fn %)))
        (= action :post) #(sign-fn (fix-keys-fn %))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn return-everything 
  "this takes a response and returns a map of the headers and the json-parsed body"
  [resp]
  
  (hash-map :headers (ac/headers resp) :body (json/read-json (ac/string resp))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn return-body
  "this takes a response and returns the json-parsed body"
  [resp]
  
  (json/read-json (ac/string resp)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn sync-http-request 
  "calls the action on the resource specified in the uri"
  [action uri &
   {:keys [query headers body oauth-creds return-fn]
    :or {oauth-creds *oauth-creds*,
         return-fn return-everything}}]
  
  (let [fix-keys #(transform-map % :key-trans fix-keyword)
        url-encode-vals #(transform-map % :val-trans url-encode-val)
        sign #(sign-query-params oauth-creds action uri :query %)

        final-query ((query-transform action sign url-encode-vals fix-keys) query)

        response-from #((action-2-function action) % :query final-query :headers headers :body body)]

    (return-fn (ac/await (response-from uri)))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn process-args 
  "takes a map of arguments, and reformats them to have stray k/v inserted in the query map"
  [arg-map]

  (let [known-arg-keys '(:query :headers :body :oauth-creds :return-fn)
        known-arg-map (select-keys arg-map known-arg-keys)
        remaining-arg-map (dissoc arg-map known-arg-keys)]
    
    (remove nil?
            (merge-with merge known-arg-map {:query remaining-arg-map}))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defmacro def-sync-twitter-method
  "declares a synchronous twitter method that is named from the resource path"
  [name action uri]

  `(defn ~name
     [& args#]
     
     (let [arg-map# (apply hash-map args#)]
       (apply sync-http-request
              ~action
              ~uri
              (reduce concat (process-args arg-map#))))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
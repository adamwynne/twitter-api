(ns twitter.core
  (:use
   [clojure.test])
  (:require
   [twitter.handlers :as hl]
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
  "takes oauth credentials and signs the query parameters"
  [oauth-creds action uri & {:keys [query]}]
  
  (oa/credentials (:consumer oauth-creds)
                  (:access-token oauth-creds)
                  (:access-token-secret oauth-creds)
                  action
                  uri
                  query))

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

(deftest test-fix-keyword
  (is (= (fix-keyword "my-test") "my_test")))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn transform-map
  "transforms the k/v pairs of a map using a supplied transformation function"
  [m & {:keys [key-trans val-trans] :or {key-trans (fn [x] x) val-trans (fn [x] x)}}]
  
  (into {} (map (fn [[k v]] [(key-trans k) (val-trans v)]) m)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn partition-map
  "partitions a map, depending on a predicate, returning a vector of maps of passes and fails"
  [map-to-partition pred]
  
  (loop [passes {}
         fails {}
         m map-to-partition]
    (if (empty? m) [passes fails]
        (let [[k v] (first m)]
          (if (pred [k v])
            (recur (assoc passes k v) fails (rest m))
            (recur passes (assoc fails k v) (rest m)))))))

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

(defn sync-http-request 
  "calls the action on the resource specified in the uri, signing with oauth in the headers"
  [action uri &
   {:keys [query headers body oauth-creds handler-fn]
    :or {oauth-creds *oauth-creds*,
         handler-fn (hl/make-default-handler)}}]

  (let [final-query (transform-map query
                                   :key-trans fix-keyword
                                   :val-trans url-encode-val)
        signing-params (sign-query-params oauth-creds action uri :query final-query)
        final-headers (merge headers {:Authorization signing-params})]
    (println "query: " final-query)
    (println "headers: " final-headers)
    (handler-fn
     (ac/await
      ((action-2-function action) uri :query final-query :headers final-headers :body body)))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn process-args 
  "takes a map of arguments, and reformats them to have unknown k/v's inserted in the query map. also
   removes the entries in the map that have a nil value"
  [arg-map]

  (let [known-arg-keys #{:query :headers :body :oauth-creds :handler-fn}
        [known-map unknown-map] (partition-map arg-map (fn [[k v]] (get known-arg-keys k)))]

    (into {} (filter second (merge-with merge known-map {:query unknown-map})))))

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

(defn make-uri 
   "makes a uri from a supplied protocol, site, version and resource-path"
   ([protocol site version resource-path]
      (str protocol "://" site "/" version "/" resource-path))
   ([protocol site resource-path]
      (str protocol "://" site "/" resource-path)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

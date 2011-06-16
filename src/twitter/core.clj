(ns twitter.core
  (:use
   [clojure.test]
   [twitter.handlers]
   [twitter.oauth])
  (:require
   [clojure.contrib.json :as json]
   [oauth.client :as oa]
   [oauth.signature :as oas]
   [http.async.client :as ac]))

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

(defn make-uri 
   "makes a uri from a supplied protocol, site, version and resource-path"
   ([protocol site version resource-path]
      (str protocol "://" site "/" version "/" resource-path))
   ([protocol site resource-path]
      (str protocol "://" site "/" resource-path)))

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

(defn sync-http-request 
  "calls the action on the resource specified in the uri, signing with oauth in the headers"
  [action uri &
   {:keys [query headers body oauth-creds handler-fn]
    :or {oauth-creds *oauth-creds*,
         handler-fn (make-default-handler)}}]

  (let [final-query (transform-map query
                                   :key-trans fix-keyword
                                   :val-trans (comp oas/url-encode str))
        signing-params (sign-query oauth-creds action uri :query final-query)
        final-headers (merge headers {:Authorization (oauth-header-string signing-params)})]

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

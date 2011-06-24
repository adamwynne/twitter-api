(ns twitter.api
  (:use
   [clojure.test]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defrecord ApiContext
    [^String protocol
     ^String host
     ^Integer version])

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn make-uri 
   "makes a uri from a supplied protocol, site, version and resource-path"
   [^ApiContext context
    ^String resource-path]

   (let [protocol (:protocol context)
         host (:host context)
         version (:version context)]
     (str protocol "://" host "/" (if version (str version "/")) resource-path)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(deftest test-make-uri
  (is (= (make-uri (ApiContext. "http" "api.twitter.com" 1) "users/show.json") "http://api.twitter.com/1/users/show.json"))
  (is (= (make-uri (ApiContext. "http" "api.twitter.com" nil) "users/show.json") "http://api.twitter.com/users/show.json")))

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

(ns twitter.oauth
  (:use
   [clojure.test])
  (:require
   [oauth.client :as oa]
   [oauth.signature :as oas]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defrecord OauthCredentials
    [consumer
     #^String access-token
     #^String access-token-secret])

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn sign-query
  "takes oauth credentials and returns a map of the signing parameters"
  [oauth-creds action uri & {:keys [query]}]

  (if oauth-creds
    (merge {:realm "Twitter API"}
           (oa/credentials (:consumer oauth-creds)
                           (:access-token oauth-creds)
                           (:access-token-secret oauth-creds)
                           action
                           uri
                           query))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(declare make-test-creds)

(deftest test-sign-query
  (let [result (sign-query (make-test-creds) :get "http://www.cnn.com" :query {:test-param "true"})]
    (is (:oauth_signature result))))
  
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn oauth-header-string 
  "creates the string for the oauth header's 'Authorization' value, url encoding each value"
  [signing-map & {:keys [url-encode?] :or {url-encode? true}}]

  (let [val-transform (if url-encode? oas/url-encode identity)
        s (reduce (fn [s [k v]] (format "%s%s=%s," s (name k) (val-transform (str v))))
                  "OAuth "
                  signing-map)]
    (.substring s 0 (dec (count s)))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(deftest test-oauth-header-string
  (is (= (oauth-header-string {:a 1 :b 2 :c 3}) "OAuth a=1,b=2,c=3"))
  (is (= (oauth-header-string {:a "hi there"}) "OAuth a=hi%20there"))
  (is (= (oauth-header-string {:a "hi there"} :url-encode? nil) "OAuth a=hi there")))

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

    (OauthCredentials. consumer access-token access-token-secret)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
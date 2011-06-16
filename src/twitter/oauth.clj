(ns twitter.oauth
  (:use
   [clojure.test])
  (:require
   [oauth.client :as oa]
   [oauth.signature :as oas]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defstruct oauth-creds
  :consumer
  :access-token
  :access-token-secret)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def *oauth-creds* (struct oauth-creds))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defmacro with-oauth-creds
  "rebinds the oauth creds to the supplied ones"
  [^oauth-creds oauth-creds & body]
  
  `(binding [*oauth-creds* ~oauth-creds]
     ~@body))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn sign-query
  "takes oauth credentials and returns a map of the signing parameters"
  [oauth-creds action uri & {:keys [query]}]

  (merge {:realm "Twitter API"}
         (oa/credentials (:consumer oauth-creds)
                         (:access-token oauth-creds)
                         (:access-token-secret oauth-creds)
                         action
                         uri
                         query)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn oauth-header-string 
  "creates the string for the oauth header's 'Authorization' value, url encoding each value"
  [signing-map]

  (let [s (reduce (fn [s [k v]] (format "%s%s=%s," s (name k) (oas/url-encode (str v))))
                  "OAuth "
                  signing-map)]
    (.substring s 0 (dec (count s)))))

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
  (let [result (sign-query (make-test-creds) :get "http://www.cnn.com" :query {:test-param "true"})]
    (is (:oauth_signature result))))
  
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(ns twitter.test.creds
  (:use
   [twitter.oauth])
  (:import
   (java.util Properties)))

(defn assert-get
  "get a value from the environment, otherwise throw an exception detailing the problem"
  [key-name]

  (or (System/getenv key-name)
      (throw (Exception. (format "please define %s in the test environment" key-name)))))

(def ^:dynamic *app-consumer-key* (assert-get "CONSUMER_KEY"))
(def ^:dynamic *app-consumer-secret* (assert-get "CONSUMER_SECRET"))
(def ^:dynamic *user-screen-name* (assert-get "SCREEN_NAME"))
(def ^:dynamic *user-access-token* (assert-get "ACCESS_TOKEN"))
(def ^:dynamic *user-access-token-secret* (assert-get "ACCESS_TOKEN_SECRET"))

(defn make-test-creds
  "makes an Oauth structure that uses an app's credentials and a users's credentials"
  []

  (make-oauth-creds *app-consumer-key*
                    *app-consumer-secret*
                    *user-access-token*
                    *user-access-token-secret*))

(defn make-app-only-test-creds
  "makes an Oauth structure that uses only an app's credentials"
  []

  (make-oauth-creds *app-consumer-key*
                    *app-consumer-secret*))

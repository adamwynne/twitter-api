(ns twitter.test.creds
  (:use
   [twitter.oauth])
  (:import
   (java.util Properties)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn load-config-file
  "this loads a config file from the classpath"
  [file-name]
  (let [file-reader (.. (Thread/currentThread)
                        (getContextClassLoader)
                        (getResourceAsStream file-name))
        props (Properties.)]
    (.load props file-reader)
    (into {} props)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def *config* (load-config-file "test.config"))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn assert-get
  "get a value from the config, otherwise throw an exception detailing the problem"
  [key-name]
  
  (or (get *config* key-name) 
      (throw (Exception. (format "please define %s in the resources/test.config file" key-name)))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def *app-consumer-key* (assert-get "app.consumer.key"))
(def *app-consumer-secret* (assert-get "app.consumer.secret"))
(def *user-screen-name* (assert-get "user.screen.name"))
(def *user-access-token* (assert-get "user.access.token"))
(def *user-access-token-secret* (assert-get "user.access.token.secret"))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn make-test-creds 
  "makes an Oauth structure that uses an app's credentials and a users's credentials"
  []

  (make-oauth-creds *app-consumer-key*
                    *app-consumer-secret*
                    *user-access-token*
                    *user-access-token-secret*))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(ns twitter.test.utils
  (:use
   [clojure.test]
   [twitter.test.creds]
   [twitter.api.restful]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defmacro is-http-code
  "checks to see if the response is a specific HTTP return code"
  [code fn-name & args]

  `(is (= (get-in (~fn-name :oauth-creds (make-test-creds) ~@args) [:status :code]) ~code)))

(defmacro is-200
  "checks to see if the response is HTTP 200"
  [fn-name & args]

  `(is-http-code 200 ~fn-name ~@args))

(defn get-user-id
  "gets the id of the supplied screen name"
  [screen-name]

  (get-in (show-user :oauth-creds (make-test-creds) :params {:screen-name screen-name})
          [:body :id]))

(defn get-current-status-id
  "gets the id of the current status for the supplied screen name"
  [screen-name]

  (get-in (show-user :oauth-creds (make-test-creds) :params {:screen-name screen-name})
          [:body :status :id]))

(defmacro with-setup-teardown
  [id-name setup teardown & body]

  `(let [~id-name ~setup]
     (try ~@body
          (finally ~teardown))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

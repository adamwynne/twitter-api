(ns twitter.handlers
  (:use
   [clojure.test])
  (:require
   [clojure.contrib.json :as json]
   [http.async.client :as ac]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn return-everything 
  "this takes a response and returns a map of the headers and the json-parsed body"
  [response]
  
  (hash-map :headers (ac/headers response)
            :status (ac/status response)
            :body (json/read-json (ac/string response))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn return-body
  "this takes a response and returns the json-parsed body"
  [response]
  
  (json/read-json (ac/string response)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn error-thrower 
  "throws the supplied error in an exception"
  [response]

  (let [error-code (:code (ac/status response))
        error-body (json/read-json (ac/string response))
        error-str (:error error-body)
        error-req (:request error-body)]
    (throw (Exception. (format "Twitter responded to request '%s' with error %d: %s"
                               error-req error-code error-str)))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn make-handler
  "creates a handler function that takes a response and reacts to success or error"
  [on-success on-error]

  (fn [response]
    (if (< (:code (ac/status response)) 400)
      (on-success response)
      (on-error response))))
  
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn make-default-handler
  "throws on error and returns the whole response to the caller"
  []

  (make-handler return-everything error-thrower))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

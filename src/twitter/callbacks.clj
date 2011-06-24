(ns twitter.callbacks
  (:use
   [clojure.test])
  (:require
   [clojure.contrib.json :as json]
   [http.async.client :as ac]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defrecord Callbacks
  [on-success
   on-error])

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn sync-return-everything 
  "this takes a response and returns a map of the headers and the json-parsed body"
  [response & {:keys [to-json?] :or {to-json? true}}]

  (let [body-trans (if to-json? json/read-json identity)]
    (hash-map :headers (ac/headers response)
              :status (ac/status response)
              :body (body-trans (ac/string response)))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn sync-return-body
  "this takes a response and returns the json-parsed body"
  [response]
  
  (json/read-json (ac/string response)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn twitter-throw
  "throws an exception populated with a message of what went wrong"
  
  ([req code desc]
     (throw (Exception. (format "Twitter responded to request '%s' with error %d: %s" req code desc))))
  ([code desc]
     (throw (Exception. (format "Twitter responded to request with error %d: %s" code desc))))
  ([desc]
     (throw (Exception. (format "Twitter responded to request with error: %s" desc))))
  ([]
     (throw (Exception. "Twitter responded to request with an unknown error"))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn sync-error-thrower 
  "throws the supplied error in an exception"
  [response]

  (let [status (ac/status response)
        body (json/read-json (ac/string response))

        desc (or (:message (first (:errors body))) (:error body)) 
        code (or (:code (first (:errors body))) (:code status))
        req (:request body)]
    
    (apply twitter-throw (remove nil? (list req code desc)))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn sync-callbacks-default
  "throws on error and returns the whole response to the caller"
  []

  (Callbacks. sync-return-everything sync-error-thrower))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn sync-callbacks-debug
  "throws on error and returns the whole response to the caller"
  []

  (let [debugger #(sync-return-everything % :to-json? false)]
    (Callbacks. debugger debugger)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn call-on-stream
  "takes a response and returns a function thats takes a chunk as input and then calls a supplied handler
   functon on the chunk"
  [chunk-handler-fn]
  
  (fn [response]
    (doseq [chunk (ac/string response)]
      (chunk-handler-fn chunk))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn streaming-callbacks-default
  "throws on error and prints out the streaming response to the caller"
  []

  (Callbacks. (call-on-stream println) #(ac/status %)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn handle-response
  "takes a response and reacts to success or error"
  [^Callbacks callbacks response]

  (if (< (:code (ac/status response)) 400)
      ((:on-success callbacks) response)
      ((:on-error callbacks) response)))
  
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(ns twitter.callbacks.handlers
  (:require [clojure.data.json :as json]
            [http.async.client :as ac]))

;;
;; The function signatures for the callbacks are:
;; (defn on-success [response])
;; - response = the success response
;;
;; (defn on-failure [response])
;; - response = the failed response with an error status (<400)
;;
;; (defn on-exception [response throwable])
;; - response = an incomplete response (up until the exception)
;; - throwable = the exception that implements the Throwable interface
;;
;; (defn on-bodypart [response baos])
;; - response = the response that has the status and headers
;; - baos = the ByteArrayOutputStream that contains a chunk of the stream
;;

(defn response-return-everything
  "this takes a response and returns a map of the headers and the json-parsed body"
  [response & {:keys [to-json?] :or {to-json? true}}]
  (-> {:headers (ac/headers response)
       :status (ac/status response)
       :body (ac/string response)}
      ; parse :body as json iff to-json? and body are truthy
      (update :body (fn [body] (cond-> body
                                 (and to-json? body) json/read-json)))))

(defn response-return-body
  "this takes a response and returns the json-parsed body"
  [response]
  (some-> (ac/string response) json/read-json))

(defn bodypart-print
  "prints out the data received from the streaming callback"
  [_ baos]
  (println (.toString ^java.io.ByteArrayOutputStream baos)))

(defn exception-print
  "prints the string version of the throwable object"
  [_ throwable]
  (println throwable))

(defn exception-rethrow
  "prints the string version of the throwable object"
  [_ throwable]
  (throw throwable))

(defn rate-limit-error?
  "returns true if the given response contains a rate limit error"
  [status]
  (= 429 (:code status)))

(defn format-rate-limit-error
  [response]
  (let [reset-time (-> response ac/headers :x-rate-limit-reset)]
    (format "Twitter responded to request with error 88: Rate limit exceeded. Next reset at %s (UTC epoch seconds)" reset-time)))

(defn get-twitter-error-message
  "interrogates a response for its twitter error message"
  [response]
  (let [status (ac/status response)
        body (response-return-body response)
        desc (or (:message (first (:errors body))) (:error body))
        code (or (:code (first (:errors body))) (:code status))
        req (:request body)]
    (cond
      (rate-limit-error? status) (format-rate-limit-error response)
      (and req code desc) (format "Twitter responded to request '%s' with error %d: %s" req code desc)
      (and code desc) (format "Twitter responded to request with error %d: %s" code desc)
      desc (format "Twitter responded to request with error: %s" desc)
      :default "Twitter responded to request with an unknown error")))

(defn response-throw-error
  "throws the supplied error in an exception"
  [response]
  (throw (ex-info (get-twitter-error-message response) {:response response})))

(defn handle-response
  "takes a response and reacts to success or error.
   'events' should be a set of keywords like #{:on-success :on-failure}"
  [response callbacks & {:keys [events] :or {events #{:on-success :on-failure}}}]
  (cond
    (and (:on-exception events)
         (ac/error response))
    ((:on-exception callbacks) response (ac/error response))
    (and (:on-success events)
         (< (:code (ac/status response)) 400))
    ((:on-success callbacks) response)
    (and (:on-failure events)
         (>= (:code (ac/status response)) 400))
    ((:on-failure callbacks) response)))

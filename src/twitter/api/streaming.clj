(ns twitter.api.streaming
  (:use
   [twitter.core])
  (:import
   (twitter.api ApiContext)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def *streaming-api* (ApiContext. "http" "stream.twitter.com" 1))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defmacro def-twitter-streaming-method
  [name action resource-path]

  `(def-twitter-method def-streaming-method ~*streaming-api* ~name ~action ~resource-path))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def-twitter-streaming-method statuses-filter :post "statuses/filter.json")
(def-twitter-streaming-method statuses-firehose	:get "statuses/firehose.json")
(def-twitter-streaming-method statuses-links :get "statuses/links.json")
(def-twitter-streaming-method statuses-retweet :get "statuses/retweets.json")
(def-twitter-streaming-method statuses-sample :get "statuses/sample.json")

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def *user-stream-api* (ApiContext. "https" "userstream.twitter.com" 2))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defmacro def-twitter-user-streaming-method
  [name action resource-path]

  `(def-twitter-method def-streaming-method ~*user-stream-api* ~name ~action ~resource-path))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def-twitter-user-streaming-method user-stream :get "user.json")

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

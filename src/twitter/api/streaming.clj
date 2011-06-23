(ns twitter.api.streaming
  (:use
   [twitter.core]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def *api-protocol* "http")
(def *api-version* 1)
(def *api-site* "stream.twitter.com")

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defmacro def-twitter-streaming-method
  [name action resource-path]

  (let [uri (make-uri *api-protocol* *api-site* *api-version* resource-path)]
    `(def-twitter-async-method ~name ~action ~uri)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def-twitter-streaming-method statuses-filter :post "statuses/filter.json")
(def-twitter-streaming-method statuses-firehose	:get "statuses/firehose.json")
(def-twitter-streaming-method statuses-links :get "statuses/links.json")
(def-twitter-streaming-method statuses-retweet :get "statuses/retweets.json")
(def-twitter-streaming-method statuses-sample :get "statuses/sample.json")

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(ns twitter.api.search
  (:use
   [twitter core callbacks])
  (:import
   (twitter.api ApiContext)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def ^:dynamic *search-api* (ApiContext. "https" "api.twitter.com" "1.1"))

(defmacro def-twitter-search-method
  "defines a search method using the search api context and the synchronous comms"
  [name verb resource-path & rest]

  `(def-twitter-method ~name ~verb ~resource-path :api ~*search-api* :callbacks (get-default-callbacks :sync :single) ~@rest))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def-twitter-search-method search :get "search/tweets.json")

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

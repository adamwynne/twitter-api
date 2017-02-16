(ns twitter.api.search
  (:require [twitter.api]
            [twitter.callbacks :refer [get-default-callbacks]]
            [twitter.core :refer [def-twitter-method]])
  (:import (twitter.api ApiContext)))

(def ^:dynamic *search-api* (ApiContext. "https" "api.twitter.com" "1.1"))

(defmacro def-twitter-search-method
  "defines a search method using the search api context and the synchronous comms"
  {:requires [#'def-twitter-method get-default-callbacks]}
  [name verb resource-path & rest]
  `(def-twitter-method ~name ~verb ~resource-path :api ~*search-api* :callbacks (get-default-callbacks :sync :single) ~@rest))

(def-twitter-search-method search :get "search/tweets.json")

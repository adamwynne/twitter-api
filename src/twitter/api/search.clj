(ns twitter.api.search
  (:use
   [twitter.core])
  (:import
   (twitter.api ApiContext)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def *search-api* (ApiContext. "http" "search.twitter.com" nil))

(defmacro def-twitter-search-method
  "defines a search method using the search api context and the synchronous comms"
  [name action resource-path & rest]

  `(def-twitter-method def-sync-method ~*search-api* ~name ~action ~resource-path ~@rest))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def-twitter-search-method search :get "search.json")

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

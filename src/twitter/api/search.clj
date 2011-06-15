(ns twitter.api.search
  (:use )
  (:require ))

(def-rest-twitter-method search
  :get
  "search.twitter.com/search.json"
)

(def-rest-twitter-method trends
  :get
  "search.twitter.com/trends.json"
)

(def-rest-twitter-method current-trends
  :get
  "search.twitter.com/trends/current.json"
)

(def-rest-twitter-method daily-trends
  :get
  "search.twitter.com/trends/daily.json"
)

(def-rest-twitter-method weekly-trends
  :get
  "search.twitter.com/trends/weekly.json"
)

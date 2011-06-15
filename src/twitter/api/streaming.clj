(ns twitter-client.streaming
  (:use )
  (:require ))


(def-twitter-streaming-method statuses-filter
	:post
	"stream.twitter.com/1/statuses/filter.json"
	[]
	[:count :delimited :follow :locations :track])

(def-twitter-streaming-method statuses-firehose
	:get
	"stream.twitter.com/1/statuses/firehose.json"
	[]
	[:count :delimited])

(def-twitter-streaming-method statuses-links
	:get
	"stream.twitter.com/1/statuses/links.json"
	[]
	[:count :delimited])

(def-twitter-streaming-method statuses-retweet
	:get
	"stream.twitter.com/1/statuses/retweets.json"
	[]
	[:delimited])

(def-twitter-streaming-method statuses-sample
	:get
	"stream.twitter.com/1/statuses/sample.json"
	[]
	[:count :delimited])
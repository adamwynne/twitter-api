(ns twitter.api.test.streaming
  (:use [clojure.test]
        [twitter.test-utils.core]
        [twitter.test creds utils]
        [twitter.callbacks]
        [twitter.api.streaming])
  (:require [http.async.client :as ac])
  (:import [twitter.callbacks.protocols AsyncStreamingCallback]))

(defn async-streaming-nop-callback
  "this callback does nothing with the results"
  []
  (AsyncStreamingCallback. (constantly nil) (constantly nil) (constantly nil)))

(deftest test-streaming
  (is-async-200 statuses-filter :params {:track "BhandMeeting"} :callbacks (async-streaming-nop-callback))
  (is-async-200 statuses-sample :callbacks (async-streaming-nop-callback)))

(deftest test-user-streaming
  (is-async-200 user-stream :callbacks (async-streaming-nop-callback)))


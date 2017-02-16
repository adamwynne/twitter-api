(ns twitter.api.test.streaming
  (:require [clojure.test :refer :all]
            [twitter.api.streaming :refer [statuses-filter
                                           statuses-sample user-stream]]
            [twitter.callbacks.protocols]
            [twitter.test-utils.core :refer [is-async-200]])
  (:import (twitter.callbacks.protocols AsyncStreamingCallback)))

(defn async-streaming-nop-callback
  "this callback does nothing with the results"
  []
  (AsyncStreamingCallback. (constantly nil) (constantly nil) (constantly nil)))

(deftest test-streaming
  (is-async-200 statuses-filter :params {:track "BhandMeeting"} :callbacks (async-streaming-nop-callback))
  (is-async-200 statuses-sample :callbacks (async-streaming-nop-callback)))

(deftest test-user-streaming
  (is-async-200 user-stream :callbacks (async-streaming-nop-callback)))

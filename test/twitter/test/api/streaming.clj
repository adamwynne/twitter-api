(ns twitter.test.api.streaming
  (:use
   [clojure.test]
   [twitter.test.creds]
   [twitter.test.utils]
   [twitter.callbacks]
   [twitter.api.streaming])
  (:require
   [http.async.client :as ac])
  (:import
   (twitter.callbacks Callbacks)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn streaming-test-callback
  []
  (let [embed-status-fn (fn [response] {:status (ac/status response)})]
    (Callbacks. embed-status-fn embed-status-fn)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(deftest test-streaming
  (is-200 statuses-filter :params {:track "BhandMeeting"} :callbacks (streaming-test-callback))
  (is-200 statuses-sample :callbacks (streaming-test-callback)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(deftest test-user-streaming
  (is-200 user-stream :callbacks (streaming-test-callback)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;


(ns twitter.test.request
  (:require [clojure.test :refer :all]
            [http.async.client.request :as req]
            [twitter.callbacks.handlers :refer [exception-rethrow]]
            [twitter.callbacks.protocols]
            [twitter.core :refer [default-client]]
            [twitter.request :refer [execute-request-callbacks]])
  (:import (twitter.callbacks.protocols AsyncSingleCallback
                                        AsyncStreamingCallback
                                        SyncSingleCallback
                                        SyncStreamingCallback)))

(deftest test-sync-single-success
  (let [p (promise)]
    (execute-request-callbacks (default-client)
                               (req/prepare-request :get "http://www.google.com")
                               (SyncSingleCallback. (fn [_] (deliver p :on-success))
                                                    (fn [_] (deliver p :on-failure))
                                                    exception-rethrow))
    (is (= @p :on-success))))

(deftest test-sync-single-failure
  (let [p (promise)]
    (execute-request-callbacks (default-client)
                               (req/prepare-request :get "http://www.google.com/willnotfindthis")
                               (SyncSingleCallback. (fn [_] (deliver p :on-success))
                                                    (fn [_] (deliver p :on-failure))
                                                    exception-rethrow))
    (is (= @p :on-failure))))

(deftest test-sync-single-exception
  (is (thrown? java.net.UnknownHostException
               (execute-request-callbacks (default-client)
                                          (req/prepare-request :get "http://www.will.throwfromthis")
                                          (SyncSingleCallback. (constantly nil)
                                                               (constantly nil)
                                                               exception-rethrow)))))

(deftest test-sync-streaming-bodypart
  (let [p (promise)]
    (execute-request-callbacks (default-client)
                               (req/prepare-request :get "http://www.google.com")
                               (SyncStreamingCallback. (fn [_ _] (deliver p :on-bodypart))
                                                       (fn [_] (deliver p :on-failure))
                                                       exception-rethrow))
    (is (= @p :on-bodypart))))

(deftest test-sync-streaming-failure
  (let [p (promise)]
    (execute-request-callbacks (default-client)
                               (req/prepare-request :get "http://www.google.com/willnotfindthis")
                               (SyncStreamingCallback. (constantly nil)
                                                       (fn [_] (deliver p :on-failure))
                                                       exception-rethrow))
    (is (= @p :on-failure))))

(deftest test-sync-streaming-exception
  (is (thrown? java.net.UnknownHostException
               (execute-request-callbacks (default-client)
                                          (req/prepare-request :get "http://www.will.throwfromthis")
                                          (SyncSingleCallback. (constantly nil)
                                                               (constantly nil)
                                                               exception-rethrow)))))

(deftest test-async-single-success
  (let [p (promise)]
    (execute-request-callbacks (default-client)
                               (req/prepare-request :get "http://www.google.com")
                               (AsyncSingleCallback. (fn [_] (deliver p :on-success))
                                                     (fn [_] (deliver p :on-failure))
                                                     (constantly nil)))
    (is (= @p :on-success))))

(deftest test-async-single-failure
  (let [p (promise)]
    (execute-request-callbacks (default-client)
                               (req/prepare-request :get "http://www.google.com/willnotfindthis")
                               (AsyncSingleCallback. (fn [_] (deliver p :on-success))
                                                     (fn [_] (deliver p :on-failure))
                                                     (constantly nil)))
    (is (= @p :on-failure))))

(deftest test-async-single-exception
  (let [p (promise)]
    (execute-request-callbacks (default-client)
                               (req/prepare-request :get "http://www.will.throwfromthis")
                               (AsyncSingleCallback. (constantly nil)
                                                     (constantly nil)
                                                     (fn [_ _] (deliver p :on-exception))))
    (is (= @p :on-exception))))

(deftest test-async-streaming-bodypart
  (let [p (promise)]
    (execute-request-callbacks (default-client)
                               (req/prepare-request :get "http://www.google.com")
                               (AsyncStreamingCallback. (fn [_ _] (deliver p :on-bodypart))
                                                        (fn [_] (deliver p :on-failure))
                                                        (fn [_ _] (deliver p :on-exception))))
    (is (= @p :on-bodypart))))

(deftest test-async-streaming-failure
  (let [p (promise)]
    (execute-request-callbacks (default-client)
                               (req/prepare-request :get "http://www.google.com/willnotfindthis")
                               (AsyncStreamingCallback. (constantly nil)
                                                        (fn [_] (deliver p :on-failure))
                                                        (fn [_ _])))
    (is (= @p :on-failure))))

(deftest test-async-streaming-exception
  (let [p (promise)]
    (execute-request-callbacks (default-client)
                               (req/prepare-request :get "http://www.will.throwfromthis")
                               (AsyncStreamingCallback. (constantly nil)
                                                        (constantly nil)
                                                        (fn [_ _] (deliver p :on-exception))))
    (is (= @p :on-exception))))

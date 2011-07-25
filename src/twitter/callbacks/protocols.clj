(ns twitter.callbacks.protocols
  (:require
   [http.async.client.request :as req]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defprotocol SuccessHandler (on-success [this response]))
(defprotocol FailureHandler (on-failure [this response]))
(defprotocol BodyPartHandler (on-bodypart [this response baos]))
(defprotocol ExceptionHandler (on-exception [this response throwable]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defrecord SyncSingleCallback
    [on-success
     on-failure]

  SuccessHandler (on-success [this response] ((:on-success this) response))
  FailureHandler (on-failure [this response] ((:on-failure this) response)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defrecord SyncStreamingCallback
    [on-bodypart
     on-failure]

  BodyPartHandler (on-bodypart [this response baos] ((:on-bodypart this) response baos))
  FailureHandler (on-failure [this response] ((:on-failure this) response)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defrecord AsyncSingleCallback
    [on-success
     on-failure
     on-exception]

  SuccessHandler (on-success [this response] ((:on-success this) response))
  FailureHandler (on-failure [this response] ((:on-failure this) response))
  ExceptionHandler (on-exception [this response throwable] ((:on-exception this) response throwable)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defrecord AsyncStreamingCallback
    [on-bodypart
     on-failure
     on-exception]

  BodyPartHandler (on-bodypart [this response baos] ((:on-bodypart this) response baos))
  FailureHandler (on-failure [this response] ((:on-failure this) response))
  ExceptionHandler (on-exception [this response throwable] ((:on-exception this) response throwable)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

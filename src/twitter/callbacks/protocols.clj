(ns twitter.callbacks.protocols
  (:use
   [twitter.callbacks.handlers])
  (:require
   [http.async.client :as ac]
   [http.async.client.request :as req]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
;; Note that the type of call can be separated into:
;;
;; - async/sync: whether the call completes synchronously or asynchronously
;; - single/streaming: whether the call results in an infinite stream or a finite response body
;;
;; The function signatures for the callbacks are:
;; (defn on-success [response])
;; - response = the success response
;;
;; (defn on-failure [response])
;; - response = the failed response with an error status (<400)
;;
;; (defn on-exception [response throwable])
;; - response = an incomplete response (up until the exception)
;; - throwable = the exception that implements the Throwable interface
;; 
;; (defn on-bodypart [response baos])
;; - response = the response that has the status and headers
;; - baos = the ByteArrayOutputStream that contains a chunk of the stream
;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defprotocol EmitCallbackList (emit-callback-list [this]))
(defprotocol AsyncSyncStatus (get-async-sync [this]))
(defprotocol SingleStreamingStatus (get-single-streaming [this]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defrecord SyncSingleCallback
    [on-success
     on-failure
     on-exception]

  AsyncSyncStatus (get-async-sync [_] :sync)
  SingleStreamingStatus (get-single-streaming [_] :single)
  
  EmitCallbackList
  (emit-callback-list
    [_]
    req/*default-callbacks*))
    
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defrecord SyncStreamingCallback
    [on-bodypart
     on-failure
     on-exception]

  AsyncSyncStatus (get-async-sync [_] :sync)
  SingleStreamingStatus (get-single-streaming [_] :streaming)

  EmitCallbackList
  (emit-callback-list
    [this]
    (merge req/*default-callbacks*
           {:part (fn [response baos] ((:on-bodypart this) response baos) [baos :continue])})))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defrecord AsyncSingleCallback
    [on-success
     on-failure
     on-exception]
  
  AsyncSyncStatus (get-async-sync [_] :async)
  SingleStreamingStatus (get-single-streaming [_] :single)

  EmitCallbackList
  (emit-callback-list
    [this]
    (merge req/*default-callbacks*
           {:completed (fn [response] (handle-response response this))
            :error (fn [response throwable] ((:on-exception this) response throwable) throwable)})))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defrecord AsyncStreamingCallback
    [on-bodypart
     on-failure
     on-exception]

  AsyncSyncStatus (get-async-sync [_] :async)
  SingleStreamingStatus (get-single-streaming [_] :streaming)

  EmitCallbackList
  (emit-callback-list
    [this]
    (merge req/*default-callbacks*
           {:completed (fn [response] (handle-response response this :events #{:on-failure}))
            :part (fn [response baos] ((:on-bodypart this) response baos) [baos :continue])
            :error (fn [response throwable] ((:on-exception this) response throwable) throwable)})))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

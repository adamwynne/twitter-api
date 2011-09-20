(ns twitter.callbacks
  (:use
   [twitter.callbacks handlers protocols])
  (:require
   [http.async.client :as ac])
  (:import
   (twitter.callbacks.protocols SyncSingleCallback SyncStreamingCallback
                                AsyncSingleCallback AsyncStreamingCallback)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
;; Some special purpose callbacks (mainly for debugging)
;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn callbacks-sync-single-print
  "just prints out whatever it gets"
  []

  (let [printer (comp println response-return-everything)]
    (SyncSingleCallback. printer
                         printer
                         exception-rethrow)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn callbacks-sync-single-debug
  "returns the whole response (un-transformed) to the caller, regardless of an error occuring"
  []

  (let [debugger #(response-return-everything % :to-json? false)]
    (SyncSingleCallback. debugger
                         debugger
                         exception-rethrow)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
;; Default callbacks
;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn callbacks-sync-single-default
  "throws on error otherwise returns the whole response to the caller"
  []

  (SyncSingleCallback. response-return-everything
                       response-throw-error
                       exception-rethrow))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn callbacks-sync-streaming-default
  "prints the exception, prints the status error or prints out the streaming response to the caller"
  []

  (SyncStreamingCallback. bodypart-print
                          response-throw-error
                          exception-rethrow))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn callbacks-async-single-default
  "prints on every state"
  []

  (AsyncSingleCallback. (comp println response-return-everything)
                        (comp println response-return-everything)
                        exception-print))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn callbacks-async-streaming-default
  "prints on every state"
  []

  (AsyncStreamingCallback. bodypart-print
                           (comp println response-return-everything)
                           exception-print))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn get-default-callbacks
  "returns the default callbacks to use for a supplied sync-type/call-type combination"
  [async-sync single-streaming]

  (case [async-sync single-streaming]
        [:sync :single] (callbacks-sync-single-default)
        [:sync :streaming] (callbacks-sync-streaming-default) 
        [:async :single] (callbacks-async-single-default) 
        [:async :streaming] (callbacks-async-streaming-default) 
        (throw (Exception. (format "unknown call/blocking-type combination: %s %s" async-sync single-streaming)))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

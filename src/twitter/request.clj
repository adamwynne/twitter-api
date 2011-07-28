(ns twitter.request
  (:use
   [twitter.callbacks]
   [twitter.callbacks protocols handlers])
  (:require
   [http.async.client.request :as req]
   [http.async.client :as ac]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn get-response-transform
  "returns a function that transforms the response into the desired outcome, depending on the request state"
  [callbacks]

  (let [async-sync (get-async-sync callbacks)
        single-streaming (get-single-streaming callbacks)]
    
    (case [async-sync single-streaming]
          [:sync :single] #(handle-response (ac/await %) callbacks :events #{:on-success :on-failure :on-exception})
          [:sync :streaming] #(handle-response (ac/await %) callbacks :events #{:on-failure :on-exception})
          [:async :single] identity ; note that if a response if passed back to a repl, the repl seems to hang
          [:async :streaming] identity)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn execute-request-callbacks
  "submits the request and then calls back to the callbacks"
  [client req callbacks]

  (let [transform (get-response-transform callbacks)
        response (apply req/execute-request
                        client
                        req
                        (apply concat (emit-callback-list callbacks)))]
    (transform response)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
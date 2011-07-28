(ns twitter.test.utils
  (:use
   [clojure.test]
   [twitter.test.creds]
   [twitter.api.restful]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defmacro is-async-200
  "checks to see if the response is HTTP return code 200, and then cancels it"
  [fn-name & args]

  `(let [response# (~fn-name :oauth-creds (make-test-creds) ~@args)]
     (try
       (try (is (= (:code (ac/status response#)) 200))
            (finally ((:cancel (meta response#)))))
       (catch java.util.concurrent.CancellationException e# nil))))

(defmacro is-http-code
  "checks to see if the response is a specific HTTP return code"
  [code fn-name & args]

  `(is (= (get-in (~fn-name :oauth-creds (make-test-creds) ~@args) [:status :code]) ~code)))

(defmacro is-200
  "checks to see if the response is HTTP 200"
  [fn-name & args]

  `(is-http-code 200 ~fn-name ~@args))

(defn get-user-id
  "gets the id of the supplied screen name"
  [screen-name]

  (get-in (show-user :oauth-creds (make-test-creds) :params {:screen-name screen-name})
          [:body :id]))

(defn get-current-status-id
  "gets the id of the current status for the supplied screen name"
  [screen-name]

  (get-in (show-user :oauth-creds (make-test-creds) :params {:screen-name screen-name})
          [:body :status :id]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn poll-until-no-error
  "repeatedly tries the poll instruction, for a maximum time, or until the error disappears"
  [poll-fn & {:keys [max-timeout-ms wait-time-ms]
              :or {max-timeout-ms 60000 wait-time-ms 10000}} ]

  (loop [curr-time-ms 0]
    (if (< curr-time-ms max-timeout-ms)
      (when-not (try (poll-fn) (catch Exception e nil))
        (Thread/sleep wait-time-ms)
        (recur (+ curr-time-ms wait-time-ms))))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defmacro with-setup-poll-teardown
  [id-name setup poll teardown & body]

  `(let [~id-name ~setup]
     (try (poll-until-no-error (fn [] ~poll))
          ~@body
          (finally ~teardown))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

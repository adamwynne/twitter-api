(ns twitter.test-utils.core
  (:require [clojure.test :refer :all]
            [http.async.client :as ac]
            [twitter.api.restful :refer [users-show]]
            [twitter.test.creds :refer [make-app-only-test-creds
                                        make-test-creds]]
            [twitter.utils :refer [assert-throw]]))

(defmacro is-async-200
  "checks to see if the response is HTTP return code 200, and then cancels it"
  {:requires [#'is]}
  [fn-name & args]
  `(let [response# (~fn-name :oauth-creds (~make-test-creds) ~@args)]
     (try
       (try (is (= (:code (~ac/status response#)) 200))
            (finally ((:cancel (meta response#)))))
       (catch java.util.concurrent.CancellationException e# nil))))

(defmacro is-http-code
  "checks to see if the response is a specific HTTP return code"
  {:requires [#'is]}
  [code fn-name & args]
  `(is (= (get-in (~fn-name :oauth-creds (~make-test-creds) ~@args) [:status :code]) ~code)))

(defmacro is-200-with-app-only
  "checks to see if the response to a request using application-only
  authentication is a specific HTTP return code"
  {:requires [#'is]}
  [fn-name & args]
  `(is (= (get-in (~fn-name :oauth-creds (~make-app-only-test-creds) ~@args) [:status :code]) 200)))

(defmacro is-200
  "checks to see if the response is HTTP 200"
  [fn-name & args]
  (if (some #{:app-only} args)
    (let [args# (remove #{:app-only} args)]
      `(is-200-with-app-only ~fn-name ~@args#))
    `(is-http-code 200 ~fn-name ~@args)))

(defn get-user-id
  "gets the id of the supplied screen name"
  [screen-name]
  (get-in (users-show :oauth-creds (make-test-creds) :params {:screen-name screen-name})
          [:body :id]))

(defn get-current-status-id
  "gets the id of the current status for the supplied screen name"
  [screen-name]
  (let [result (users-show :oauth-creds (make-test-creds) :params {:screen-name screen-name})]
    (assert-throw (get-in result [:body :status :id])
                  "could not retrieve the user's profile in 'show-user'")))

(defn poll-until-no-error
  "repeatedly tries the poll instruction, for a maximum time, or until the error disappears"
  [poll-fn & {:keys [max-timeout-ms wait-time-ms]
              :or {max-timeout-ms 60000 wait-time-ms 10000}}]
  (loop [curr-time-ms 0]
    (if (< curr-time-ms max-timeout-ms)
      (when-not (try (poll-fn) (catch Exception e nil))
        (Thread/sleep wait-time-ms)
        (recur (+ curr-time-ms wait-time-ms))))))

(defmacro with-setup-poll-teardown
  [id-name setup poll teardown & body]
  `(let [~id-name ~setup]
     (try (poll-until-no-error (fn [] ~poll))
          ~@body
          (finally ~teardown))))

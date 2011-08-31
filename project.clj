(defproject twitter-api/twitter-api "0.4.0"
  :description "full twitter interface"
  :dev-dependencies [[org.slf4j/slf4j-simple "1.6.1"]
                     [lein-clojars "0.6.0"]
                     [swank-clojure "1.3.2"]]
;  :jvm-opts ["-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=8000"]
  :dependencies [[org.clojure/clojure "1.2.1"]
                 [org.clojure/clojure-contrib "1.2.0"]
                 [http.async.client "0.3.1"]
                 [clj-oauth "1.2.10-SNAPSHOT"]])

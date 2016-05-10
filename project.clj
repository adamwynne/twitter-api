(defproject twitter-api/twitter-api "0.7.8"
  :description "full twitter api async interface"
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/data.json "0.2.6"]
                 [org.clojure/data.codec "0.1.0"]
                 [http.async.client "0.5.2"]
                 [clj-oauth "1.5.5"]]
  :url "https://github.com/adamwynne/twitter-api"
  :scm {:name "git"
        :url "https://github.com/adamwynne/twitter-api"}
  :license {:name "Apache License, Version 2.0"
            :url "http://www.apache.org/license/LICENSE-2.0.html"}
  :deploy-repositories [["clojars" {:sign-releases false}]]
  :min-lein-version "2.0.0")

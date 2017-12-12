(ns twitter.api
  (:require [clojure.string :as str]))

(defrecord ApiContext [protocol host version])

(def ^:deprecated make-api-context ->ApiContext)

(defn clean-resource-path
  "convert groups of symbols to single dashes and drop trailing dashes"
  [resource-path]
  (-> resource-path
      (str/replace #"[^a-zA-Z]+" "-")
      (str/replace #"-$" "")))

(defn make-uri
  "makes a uri from a supplied protocol, site, version and resource-path"
  [context resource-path]
  (let [{:keys [protocol host version]} context]
    (str protocol "://" host "/" (when version (str version "/")) resource-path)))

(defn subs-uri
  "substitutes parameters for tokens in the uri"
  [uri params]
  (loop [matches (re-seq #"\{\:(\w+)\}" uri)
         result uri]
    (if (empty? matches)
      result
      (let [[token kw] (first matches)
            value (get params (keyword kw))]
        (when-not value
          (throw (ex-info (format "%s needs :%s param to be supplied" uri kw) {:uri uri :kw kw})))
        (recur (rest matches) (str/replace result token (str value)))))))

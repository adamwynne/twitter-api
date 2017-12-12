(ns twitter.utils
  (:require [clojure.string :as str]))

(defn transform-map
  "transforms the k/v pairs of a map using a supplied transformation function"
  [map-to-transform & {:keys [key-trans val-trans] :or {key-trans identity val-trans identity}}]
  (when map-to-transform
    (into {} (map (fn [[k v]] [(key-trans k) (val-trans v)]) map-to-transform))))

(defn get-file-ext
  "retrieves the file extension portion from the filename"
  [filename]
  (let [dot-index (or (str/last-index-of filename \.) -1)]
    (when (pos? dot-index)
      (let [suffix (subs filename (inc dot-index))]
        (when (pos? (count suffix))
          suffix)))))

(defn classpath-file
  "this loads a file from the classpath and returns an input stream"
  [file-name]
  (or (.. (Thread/currentThread)
          (getContextClassLoader)
          (getResource file-name)
          (getFile))
      (throw (ex-info (format "Cannot get file %s as resource" file-name)
                      {:file-name file-name}))))

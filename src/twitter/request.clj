(ns twitter.request
  (:require [clojure.string :as string]
            [http.async.client :as ac]
            [http.async.client.request :as req]
            [http.async.client.util :as requ]
            [twitter.callbacks.handlers :refer [handle-response]]
            [twitter.callbacks.protocols :refer [emit-callback-list
                                                 get-async-sync
                                                 get-single-streaming]]
            [twitter.utils :refer [get-file-ext]]
            [clojure.java.io :as io])
  (:import (com.ning.http.client RequestBuilder)
           (com.ning.http.client.cookie Cookie)
           (com.ning.http.client.multipart FilePart StringPart)
           (java.io File InputStream)))

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

(defn execute-request-callbacks
  "submits the request and then calls back to the callbacks"
  [client req callbacks]
  (let [transform (get-response-transform callbacks)
        response (apply req/execute-request
                        client
                        req
                        (apply concat (emit-callback-list callbacks)))]
    (transform response)))

(defn- add-to-req
  [rb kvs f]
  (doseq [[k v] kvs] (f rb
                        (if (keyword? k) (name k) k)
                        (if (coll? v)
                          (string/join "," v)
                          (str v)))))

(defn- add-headers
  "adds the headers to the requestbuilder"
  [rb headers]
  (add-to-req rb headers #(.addHeader %1 %2 %3)))

(defn- add-cookies
  "adds the cookies to the requestbuilder"
  [rb cookies]
  (doseq [{:keys [name
                  value
                  wrap
                  domain
                  path
                  max-age
                  secure
                  http-only]
           :or {path "/"
                wrap false
                max-age 30
                secure false
                http-only false}} cookies]
    (.addCookie rb (Cookie. name value wrap domain path max-age secure http-only))))

(defn- add-query-parameters
  "adds the query parameters to the requestbuilder"
  [rb query]
  (add-to-req rb query #(.addQueryParam %1 %2 %3)))

(defn guess-mime-type
  "guesses the MIME type of a media file"
  [file-name]
  (condp some [(.toLowerCase (get-file-ext file-name))]
    #{"jpg" "jpeg"} "image/jpeg"
    #{"png"} "image/png"
    #{"gif"} "image/gif"
    #{"webp"} "image/webp"
    #{"mp4"} "video/mp4"
    #{"mov"} "video/quicktime"
    (throw (Exception. (format "unable to guess MIME type of file: %s" file-name)))))

(defn file-body-part
  "takes a filename and returns a 'Part' object that can be added to the request"
  [file-name]
  (let [item-name "media[]"
        file (File. file-name)]
    (FilePart. item-name file (guess-mime-type file-name))))

(defn status-body-part
  "takes a filename and returns a 'Part' object that can be added to the request"
  [status]
  (StringPart. "status" status))

(defn- add-body
  "adds the body (or sequence of bodies) onto the request builder, dealing with the special cases"
  [rb body content-type]
  (cond
    (= "multipart/form-data" content-type) (doseq [bp (if (coll? body) body (list body))]
                                             (.addBodyPart rb bp))
    (map? body) (doseq [[k v] body]
                  (.addFormParam rb
                                 (if (keyword? k) (name k) k)
                                 (str v)))

    (string? body) (.setBody rb (.getBytes (if (= "application/x-www-form-urlencoded" content-type)
                                             (req/url-encode body)
                                             body)
                                           "UTF-8"))

    (instance? InputStream body) (.setBody rb body)

    (instance? File body) (.setBody rb body)))

(defn- set-timeout
  "sets the timeout for the request"
  [rb timeout]
  (.setRequestTimeout rb timeout))

(defn prepare-request-with-multi
  "the same as a normal prepare-request, but deals with multi-part form-data as a content-type"
  [method #^String url & {:keys [headers
                                 query
                                 body
                                 cookies
                                 proxy
                                 auth
                                 timeout]}]
  (let [rb (RequestBuilder. (req/convert-method method))]
    (when headers (add-headers rb headers))
    (when query (add-query-parameters rb query))
    (when body (add-body rb body (:content-type headers)))
    (when cookies (add-cookies rb cookies))
    (when auth (requ/set-realm auth rb))
    (when proxy (requ/set-proxy proxy rb))
    (when timeout (set-timeout rb timeout))
    (.. rb (setUrl url) (build))))

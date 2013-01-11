(ns twitter.oauth
  (:use
   [clojure.test])
  (:require
   [oauth.client :as oa]
   [oauth.signature :as oas]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

 (defrecord OauthCredentials
    [consumer
     #^String access-token
     #^String access-token-secret])

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn sign-query
  "takes oauth credentials and returns a map of the signing parameters"
  [#^OauthCredentials oauth-creds verb uri & {:keys [query]}]

  (if oauth-creds
    (into (sorted-map)
          (merge {:realm "Twitter API"}
                 (oa/credentials (:consumer oauth-creds)
                                 (:access-token oauth-creds)
                                 (:access-token-secret oauth-creds)
                                 verb
                                 uri
                                 query)))))
  
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn oauth-header-string 
  "creates the string for the oauth header's 'Authorization' value, url encoding each value"
  [signing-map & {:keys [url-encode?] :or {url-encode? true}}]

  (let [val-transform (if url-encode? oas/url-encode identity)
        s (reduce (fn [s [k v]] (format "%s%s=\"%s\"," s (name k) (val-transform (str v))))
                  "OAuth "
                  signing-map)]
    (.substring s 0 (dec (count s)))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn make-oauth-creds
  "creates an oauth object out of supplied params"
  [app-key app-secret user-token user-token-secret]

  (let [consumer (oa/make-consumer app-key
                                   app-secret
                                   "https://twitter.com/oauth/request_token"
                                   "https://twitter.com/oauth/access_token"
                                   "https://twitter.com/oauth/authorize"
                                   :hmac-sha1)]
        
    (OauthCredentials. consumer user-token user-token-secret)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

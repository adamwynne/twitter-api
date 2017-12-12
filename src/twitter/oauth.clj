(ns twitter.oauth
  (:require [clojure.data.codec.base64 :as b64]
            [http.async.client :refer [create-client]]
            [http.async.client.request :as req]
            [oauth.client :as oa]
            [oauth.signature :as oas]
            [twitter.callbacks :refer [callbacks-sync-single-default]]
            [twitter.request :refer [execute-request-callbacks]]))

(defrecord OauthCredentials [consumer access-token access-token-secret])

(defn sign-query
  "takes oauth credentials and returns a map of the signing parameters"
  [oauth-creds verb uri & {:keys [query]}]
  (when-let [{:keys [consumer access-token access-token-secret]} oauth-creds]
    (assoc (oa/credentials consumer access-token access-token-secret verb uri query) :realm "Twitter API")))

(defn oauth-header-string
  "Creates the string for the oauth header's 'Authorization' value,
  url encoding each value. If the signing-map is an application-only
  token, returns the 'Bearer' value."
  [signing-map & {:keys [url-encode?] :or {url-encode? true}}]
  (if-let [app-only-token (:bearer signing-map)]
    (str "Bearer " app-only-token)
    (let [val-transform (if url-encode? oas/url-encode identity)
          s (reduce (fn [s [k v]] (format "%s%s=\"%s\"," s (name k) (val-transform (str v))))
                    "OAuth "
                    (apply hash-map (flatten (reverse signing-map))))]
      (subs s 0 (dec (count s))))))

(defn- encode-app-only-key
  "Given a consumer-key and consumer-secret, concatenates and Base64
  encodes them so that they can be submitted to Twitter in exchange
  for an application-only token."
  [consumer-key consumer-secret]
  (let [concat-keys (str (oas/url-encode consumer-key) ":" (oas/url-encode consumer-secret))
        base64-bytes (b64/encode (.getBytes concat-keys))]
    (String. ^bytes base64-bytes "UTF-8")))

(defn request-app-only-token
  [consumer-key consumer-secret]
  (let [auth-string (str "Basic " (encode-app-only-key consumer-key consumer-secret))
        content-type "application/x-www-form-urlencoded;charset=UTF-8"
        req (req/prepare-request :post, "https://api.twitter.com/oauth2/token"
                                 :headers {"Authorization" auth-string
                                           "Content-Type" content-type}
                                 :body "grant_type=client_credentials")
        client (create-client :follow-redirects false :request-timeout -1)
        {:keys [status body]} (execute-request-callbacks client req (callbacks-sync-single-default))]
    (if (= (:code status) 200)
      {:bearer (:access_token body)}
      (throw (ex-info (str "Failed to retrieve application-only due to an unknown error: " body)
                      {:body body})))))

(defn make-oauth-creds
  "Creates an oauth object out of supplied params. If only an app-key
  and app-secret are supplied, this function will return an
  application-only authentication token. If a user-key and
  user-token-secret are also supplied, then it will return a fully
  authenticated token."
  ([app-key app-secret]
   (request-app-only-token app-key app-secret))
  ([app-key app-secret user-token user-token-secret]
   (let [consumer (oa/make-consumer app-key
                                    app-secret
                                    "https://twitter.com/oauth/request_token"
                                    "https://twitter.com/oauth/access_token"
                                    "https://twitter.com/oauth/authorize"
                                    :hmac-sha1)]
     (OauthCredentials. consumer user-token user-token-secret))))

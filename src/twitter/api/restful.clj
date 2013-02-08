(ns twitter.api.restful
  (:use
   [twitter core callbacks api])
  (:import
   (twitter.api ApiContext)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def ^:dynamic *rest-api* (make-api-context "http" "api.twitter.com" "1.1"))
(def ^:dynamic *oauth-api* (make-api-context "https" "api.twitter.com"))
(def ^:dynamic *rest-upload-api* (make-api-context "http" "upload.twitter.com" 1))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defmacro def-twitter-restful-method
  [verb resource-path & rest]
  (let [json-path (str resource-path ".json") ; v1.1 is .json only.
        dashed-name (clojure.string/replace resource-path #"[^a-zA-Z]+" "-") ; convert group of symbols to a dash 
        clean-name (clojure.string/replace dashed-name #"-$" "") ; drop trailing dashes
        fn-name (symbol clean-name)]
    `(def-twitter-method ~fn-name ~verb ~json-path :api ~*rest-api* :callbacks (get-default-callbacks :sync :single) ~@rest)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;


;; Accounts
(def-twitter-restful-method :get  "account/settings")
(def-twitter-restful-method :get  "account/verify_credentials")
(def-twitter-restful-method :post "account/settings") 
(def-twitter-restful-method :post "account/update_delivery_device")
(def-twitter-restful-method :post "account/update_profile")
(def-twitter-restful-method :post "account/update_profile_background_image")
(def-twitter-restful-method :post "account/update_profile_colors")
(def-twitter-restful-method :post "account/update_profile_image")
(def-twitter-restful-method :post "account/remove_profile_banner")
(def-twitter-restful-method :post "account/update_profile_banner")
(def-twitter-restful-method :post "users/profile_banner")
(def-twitter-restful-method :get  "application/rate_limit_status")

;; Blocks
(def-twitter-restful-method :get  "blocks/list")
(def-twitter-restful-method :get  "blocks/ids")
(def-twitter-restful-method :post "blocks/create")
(def-twitter-restful-method :post "blocks/destroy")

;; Timeline
(def-twitter-restful-method :get "statuses/mentions_timeline")
(def-twitter-restful-method :get "statuses/user_timeline")
(def-twitter-restful-method :get "statuses/home_timeline")
(def-twitter-restful-method :get "statuses/retweets_of_me")

;; Statuses
(def-twitter-restful-method :get  "statuses/retweets/{:id}")
(def-twitter-restful-method :get  "statuses/show/{:id}")
(def-twitter-restful-method :post "statuses/destroy/{:id}")
(def-twitter-restful-method :post "statuses/update")
(def-twitter-restful-method :post "statuses/retweet/{:id}")
(def-twitter-restful-method :get  "statuses/oembed")
; Supply the status and file to the :body as a sequence using the functions 'file-body-part' and 'status-body-part'
; i.e. :body [(file-body-part "/pics/mypic.jpg") (status-body-part "hello world")]
; for an example, see twitter.test.file-upload
(def-twitter-restful-method :post "statuses/update_with_media"
                            :api *rest-upload-api*
                            :headers {:content-type "multipart/form-data"})

;; Search
(def-twitter-restful-method :get "search/tweets")

;; User
(def-twitter-restful-method :get "users/show")
(def-twitter-restful-method :get "users/lookup")
(def-twitter-restful-method :get "users/search")
(def-twitter-restful-method :get "users/contributees")
(def-twitter-restful-method :get "users/contributors")
(def-twitter-restful-method :get "users/suggestions")
(def-twitter-restful-method :get "users/suggestions/{:slug}")
(def-twitter-restful-method :get "users/suggestions/{:slug}/members")

;; Trends
(def-twitter-restful-method :get "trends/place")
(def-twitter-restful-method :get "trends/available")
(def-twitter-restful-method :get "trends/closest")


;; Oauth
(def-twitter-restful-method :get  "oauth/authenticate" :api *oauth-api*)
(def-twitter-restful-method :get  "oauth/authorize" :api *oauth-api*)
(def-twitter-restful-method :post "oauth/access_token" :api *oauth-api*)
(def-twitter-restful-method :post "oauth/request_token" :api *oauth-api*)

;; Lists
(def-twitter-restful-method :get  "lists/list")
(def-twitter-restful-method :get  "lists/statuses")
(def-twitter-restful-method :get  "lists/show")
(def-twitter-restful-method :get  "lists/memberships")
(def-twitter-restful-method :get  "lists/subscriptions")
(def-twitter-restful-method :post "lists/create")
(def-twitter-restful-method :post "lists/update")
(def-twitter-restful-method :post "lists/destroy")

;; List members
(def-twitter-restful-method :post "lists/members/destroy")
(def-twitter-restful-method :post "lists/members/destroy_all")
(def-twitter-restful-method :get  "lists/members")
(def-twitter-restful-method :get  "lists/members/show")
(def-twitter-restful-method :post "lists/members/create")
(def-twitter-restful-method :post "lists/members/create_all")

;; List subscribers
(def-twitter-restful-method :get  "lists/subscribers")
(def-twitter-restful-method :get  "lists/subscribers/show")
(def-twitter-restful-method :post "lists/subscribers/create")
(def-twitter-restful-method :post "lists/subscribers/destroy")

;; Direct messages
(def-twitter-restful-method :get  "direct_messages")
(def-twitter-restful-method :get  "direct_messages/sent")
(def-twitter-restful-method :get  "direct_messages/show")
(def-twitter-restful-method :post "direct_messages/new")
(def-twitter-restful-method :post "direct_messages/destroy")

;; Friendships
(def-twitter-restful-method :get  "friendships/lookup")
(def-twitter-restful-method :post "friendships/create")
(def-twitter-restful-method :post "friendships/destroy")
(def-twitter-restful-method :post "friendships/update")
(def-twitter-restful-method :get  "friendships/show")
(def-twitter-restful-method :get  "friendships/incoming")
(def-twitter-restful-method :get  "friendships/outgoing")

;; Friends and followers
(def-twitter-restful-method :get "friends/ids")
(def-twitter-restful-method :get "friends/list")
(def-twitter-restful-method :get "followers/ids")
(def-twitter-restful-method :get "followers/list")

;; Favourites
(def-twitter-restful-method :get  "favorites/list")
(def-twitter-restful-method :post "favorites/destroy")
(def-twitter-restful-method :post "favorites/create")

;; Report spam
(def-twitter-restful-method :post "users/report_spam") 

;; Saved searches
(def-twitter-restful-method :get  "saved_searches/list")
(def-twitter-restful-method :get  "saved_searches/show/{:id}")
(def-twitter-restful-method :post "saved_searches/create")
(def-twitter-restful-method :post "saved_searches/destroy/{:id}")

;; Geo
(def-twitter-restful-method :get  "geo/id/{:place_id}")
(def-twitter-restful-method :get  "geo/reverse_geocode")
(def-twitter-restful-method :get  "geo/search")
(def-twitter-restful-method :get  "geo/similar_places")
(def-twitter-restful-method :post "geo/place")


;; Help
(def-twitter-restful-method :get "help/configuration")
(def-twitter-restful-method :get "help/languages")
(def-twitter-restful-method :get "help/tos")
(def-twitter-restful-method :get "help/privacy")


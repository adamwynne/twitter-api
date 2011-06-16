(ns twitter.api.restful
  (:use
   [twitter.core]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def *api-protocol* "http")
(def *api-version* 1)
(def *api-site* "api.twitter.com")

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defmacro def-rest-twitter-method
  [name action resource-path]

  (let [uri (make-uri *api-protocol* *api-site* *api-version* resource-path)]
    `(def-sync-twitter-method ~name ~action ~uri)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def-rest-twitter-method public-timeline :get "statuses/public_timeline.json")
(def-rest-twitter-method friends-timeline :get "statuses/friends_timeline.json")
(def-rest-twitter-method user-timeline :get "statuses/user_timeline.json")
(def-rest-twitter-method home-timeline :get "statuses/home_timeline.json")
(def-rest-twitter-method mentions :get "statuses/mentions.json")
(def-rest-twitter-method show-status :get "statuses/show.json")
(def-rest-twitter-method update-status :post "statuses/update.json")
(def-rest-twitter-method destroy-status :post "statuses/destroy.json")

(def-rest-twitter-method show-user :get "users/show.json")
(def-rest-twitter-method lookup-users :get "users/lookup.json")
(def-rest-twitter-method search-users :get "users/search.json")
(def-rest-twitter-method suggest-slugs :get "users/suggestions.json")
(def-rest-twitter-method suggest-users-for-slug :get "users/suggestions/:slug.json")

(def-rest-twitter-method direct-messages :get "direct_messages.json")
(def-rest-twitter-method sent-direct-messages :get "direct_messages/sent.json")
(def-rest-twitter-method send-direct-message :post "direct_messages/new.json")
(def-rest-twitter-method destroy-direct-message :post "direct_messages/destroy.json")

(def-rest-twitter-method create-friendship :post "friendships/create.json")
(def-rest-twitter-method destroy-friendship :post "friendships/destroy.json")
(def-rest-twitter-method show-friendship :get "friendships/show.json")

(def-rest-twitter-method friends-of :get "friends/ids.json")

(def-rest-twitter-method followers-of :get "followers/ids.json")

(def-rest-twitter-method verify-credentials :get "account/verify_credentials.json")
(def-rest-twitter-method rate-limit-status :get "account/rate_limit_status.json")
(def-rest-twitter-method end-session :post "account/end_session.json")
(def-rest-twitter-method update-profile :post "account/update_profile.json")
(def-rest-twitter-method update-delivery-device :post "account/update_delivery_device.json")
(def-rest-twitter-method update-profile-colors :post "account/update_profile_colors.json")
(def-rest-twitter-method update-profile-image :post "account/update_profile_image.json")
(def-rest-twitter-method update-profile-background-image :post "account/update_profile_background_image.json")

(def-rest-twitter-method favorites :get "favorites.json")
(def-rest-twitter-method create-favorite :post "favorites/create.json")
(def-rest-twitter-method destroy-favorite :post "favorites/destroy.json")

(def-rest-twitter-method notifications-follow :post "notifications/follow.json")
(def-rest-twitter-method notifications-leave :post "notifications/leave.json")

(def-rest-twitter-method create-block :post "blocks/create.json")
(def-rest-twitter-method destroy-block :post "blocks/destroy.json")
(def-rest-twitter-method block-exists :get "blocks/exists.json")
(def-rest-twitter-method blocking-users :get "blocks/blocking.json")
(def-rest-twitter-method blocking-user-ids :get "blocks/blocking/ids.json")

(def-rest-twitter-method saved-searches :get "saved_searches.json")
(def-rest-twitter-method show-saved-search :get "saved_searches/show.json")
(def-rest-twitter-method create-saved-search :post "saved_searches/create.json")
(def-rest-twitter-method destroy-saved-search :post "saved_searches/destroy.json")


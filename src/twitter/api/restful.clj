(ns twitter.api.restful
  (:use
   [twitter.core]
   [twitter.oauth]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def *api-protocol* "http")
(def *api-version* 1)
(def *api-site* "api.twitter.com")

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defmacro def-twitter-rest-method
  [name action resource-path]

  (let [uri (make-uri *api-protocol* *api-site* *api-version* resource-path)]
    `(def-twitter-sync-method ~name ~action ~uri)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; Accounts
(def-twitter-rest-method verify-credentials :get "account/verify_credentials.json")
(def-twitter-rest-method rate-limit-status :get "account/rate_limit_status.json")
(def-twitter-rest-method account-totals :get "account/totals.json")
(def-twitter-rest-method account-settings :get "account/settings.json")
(def-twitter-rest-method update-account-settings :post "account/settings.json") 
(def-twitter-rest-method update-profile-colors :post "account/update_profile_colors.json")
(def-twitter-rest-method update-profile-image :post "account/update_profile_image.json")
(def-twitter-rest-method update-profile-background-image :post "account/update_profile_background_image.json")
(def-twitter-rest-method update-profile :post "account/update_profile.json")

;; Blocks
(def-twitter-rest-method create-block :post "blocks/create.json")
(def-twitter-rest-method destroy-block :post "blocks/destroy.json")
(def-twitter-rest-method block-exists :get "blocks/exists.json")
(def-twitter-rest-method blocking-users :get "blocks/blocking.json")
(def-twitter-rest-method blocking-user-ids :get "blocks/blocking/ids.json")

;; Timeline
(def-twitter-rest-method public-timeline :get "statuses/public_timeline.json")
(def-twitter-rest-method home-timeline :get "statuses/home_timeline.json")
(def-twitter-rest-method user-timeline :get "statuses/user_timeline.json")
(def-twitter-rest-method mentions :get "statuses/mentions.json")
(def-twitter-rest-method retweeted-by-me :get "statuses/retweeted_by_me.json")
(def-twitter-rest-method retweeted-to-me :get "statuses/retweeted_to_me.json")
(def-twitter-rest-method retweets-of-me :get "statuses/retweets_of_me.json")

;; Tweets
(def-twitter-rest-method show-status :get "statuses/show/{:id}.json")
(def-twitter-rest-method update-status :post "statuses/update.json")
(def-twitter-rest-method destroy-status :post "statuses/destroy/{:id}.json")
(def-twitter-rest-method retweet-status :post "statuses/retweet/{:id}.json")
(def-twitter-rest-method show-retweets :get "statuses/retweets/{:id}.json")
(def-twitter-rest-method retweeted-by :get "statuses/{:id}/retweeted_by.json")
(def-twitter-rest-method retweeted-by-ids :get "statuses/{:id}/retweeted_by/ids.json")

;; User
(def-twitter-rest-method show-user :get "users/show.json")
(def-twitter-rest-method lookup-users :get "users/lookup.json")
(def-twitter-rest-method search-users :get "users/search.json")
(def-twitter-rest-method suggest-slugs :get "users/suggestions.json")
(def-twitter-rest-method suggest-users-for-slug :get "users/suggestions/{:slug}.json")
;(def-twitter-rest-method profile-image-for-user :get "users/profile_image/{:screen_name}.format")
(def-twitter-rest-method show-contributors :get "users/contributors.json")
(def-twitter-rest-method show-contributees :get "users/contributees.json")

;; Trends
(def-twitter-rest-method trends :get "trends.json") 
(def-twitter-rest-method current-trends :get "trends/current.json") 
(def-twitter-rest-method daily-trends :get "trends/daily.json")
(def-twitter-rest-method weekly-trends :get "trends/weekly.json")

;; Local trends
(def-twitter-rest-method location-trends :get "trends/available.json")
(def-twitter-rest-method location-trend :get "trends/{:woeid}.json")

;; Lists
(def-twitter-rest-method show-lists :get "lists.json")
(def-twitter-rest-method show-list :get "lists/show.json")
(def-twitter-rest-method list-memberships :get "lists/memberships.json")
(def-twitter-rest-method list-subscriptions :get "lists/subscriptions.json")
(def-twitter-rest-method list-statuses :get "lists/statuses.json")
(def-twitter-rest-method create-list :post "lists/create.json")
(def-twitter-rest-method update-list :post "lists/update.json")
(def-twitter-rest-method destroy-list :post "lists/destroy.json")

;; List members
(def-twitter-rest-method list-members :get "lists/members.json")
(def-twitter-rest-method check-member :get "lists/members/show.json")
(def-twitter-rest-method add-member :post "lists/members/create.json")
(def-twitter-rest-method add-members :post "lists/members/create_all.json")
(def-twitter-rest-method remove-member :post "lists/members/destroy.json")

;; List subscribers
(def-twitter-rest-method list-subscribers :get "lists/subscribers.json")
(def-twitter-rest-method check-subscriber :get "lists/subscribers/show.json")
(def-twitter-rest-method add-subscriber :post "lists/subscribers/create.json")
(def-twitter-rest-method remove-subscriber :post "lists/subscribers/destroy.json")

;; Direct messages
(def-twitter-rest-method direct-messages :get "direct_messages.json")
(def-twitter-rest-method sent-direct-messages :get "direct_messages/sent.json")
(def-twitter-rest-method send-direct-message :post "direct_messages/new.json")
(def-twitter-rest-method destroy-direct-message :post "direct_messages/destroy/{:id}.json")

;; Friendships
(def-twitter-rest-method create-friendship :post "friendships/create.json")
(def-twitter-rest-method destroy-friendship :post "friendships/destroy.json")
(def-twitter-rest-method check-friendship :post "friendships/exists.json")
(def-twitter-rest-method show-friendship :get "friendships/show.json")
(def-twitter-rest-method incoming-friendship :get "friendships/incoming.json")
(def-twitter-rest-method outgoing-friendship :get "friendships/outgoing.json")

;; Friends and followers
(def-twitter-rest-method show-friends :get "friends/ids.json")
(def-twitter-rest-method show-followers :get "followers/ids.json")

;; Favourites
(def-twitter-rest-method favourites :get "favorites.json")
(def-twitter-rest-method create-favourite :post "favorites/create/{:id}.json")
(def-twitter-rest-method destroy-favourite :post "favorites/destroy/{:id}.json")

;; Notifications
(def-twitter-rest-method notifications-follow :post "notifications/follow.json")
(def-twitter-rest-method notifications-leave :post "notifications/leave.json")

;; Report spam
(def-twitter-rest-method report-spam :post "report_spam.json") 

;; Saved searches
(def-twitter-rest-method saved-searches :get "saved_searches.json")
(def-twitter-rest-method show-saved-search :get "saved_searches/show/{:id}.json")
(def-twitter-rest-method create-saved-search :post "saved_searches/create.json")
(def-twitter-rest-method destroy-saved-search :post "saved_searches/destroy/{:id}.json")

;; Geo
(def-twitter-rest-method search-places :get "geo/search.json")
(def-twitter-rest-method similar-places :get "geo/similar_places.json")
(def-twitter-rest-method reverse-geocode :get "geo/reverse_geocode.json")
(def-twitter-rest-method show-place :get "geo/id/{:place_id}.json")
(def-twitter-rest-method create-place :post "geo/place.json")

;; Legal
(def-twitter-rest-method legal-tos :get "legal/tos.json")
(def-twitter-rest-method legal-privacy :get "legal/privacy.json")

;; Help
(def-twitter-rest-method help-test :get "help/test.json")
(def-twitter-rest-method help-configuration :get "help/configuration.json")
(def-twitter-rest-method help-languages :get "help/languages.json")
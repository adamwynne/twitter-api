(ns twitter-client.restful
  (:use )
  (:require ))


(def-twitter-method public-timeline
  :get
  "api.twitter.com/1/statuses/public_timeline.json"
  []
  []
  (comp #(:content %) status-handler))

(def-twitter-method friends-timeline
  :get
  "api.twitter.com/1/statuses/friends_timeline.json"
  []
  [:since-id
   :max-id
   :count
   :page]
  (comp #(:content %) status-handler))

(def-twitter-method user-timeline
  :get
  "api.twitter.com/1/statuses/user_timeline.json"
  []
  [:id
   :user-id
   :screen-name
   :since-id
   :max-id
   :count
   :page]
  (comp #(:content %) status-handler))

(def-twitter-method home-timeline
  :get
  "api.twitter.com/1/statuses/home_timeline.json"
  []
  [:since-id
   :max-id
   :count
   :page
   :skip-user
   :include-entities]
  (comp #(:content %) status-handler))

(def-twitter-method mentions
  :get
  "api.twitter.com/1/statuses/mentions.json"
  []
  [:since-id
   :max-id
   :count
   :page]
  (comp #(:content %) status-handler))

(def-twitter-method show-status
  :get
  "api.twitter.com/1/statuses/show.json"
  [:id]
  []
  (comp #(:content %) status-handler))

(def-twitter-method update-status
  :post
  "api.twitter.com/1/statuses/update.json"
  [:status]
  [:in-reply-to-status-id]
  (comp #(:status (:content %)) status-handler))

(def-twitter-method destroy-status
  :post
  "api.twitter.com/1/statuses/destroy.json"
  [:id]
  []
  (comp #(:status (:content %)) status-handler))

(def-twitter-method show-user-by-id
  :get
  "api.twitter.com/1/users/show.json"
  [:user-id]
  []
  (comp #(:content %) status-handler))

(def-twitter-method show-user-by-name
  :get
  "api.twitter.com/1/users/show.json"
  [:screen-name]
  []
  (comp #(:content %) status-handler))

(def-twitter-method lookup-users-by-id
  :get
  "api.twitter.com/1/users/lookup.json"
  [:user-id]
  []
  (comp #(:content %) status-handler))

(def-twitter-method lookup-users-by-name
  :get
  "api.twitter.com/1/users/lookup.json"
  [:screen-name]
  []
  (comp #(:content %) status-handler))

(def-twitter-method direct-messages
  :get
  "api.twitter.com/1/direct_messages.json"
  []
  [:since-id
   :max-id
   :count
   :page]
  (comp #(:content %) status-handler))

(def-twitter-method sent-direct-messages
  :get
  "api.twitter.com/1/direct_messages/sent.json"
  []
  [:since-id
   :max-id
   :count
   :page]
  (comp #(:content %) status-handler))

(def-twitter-method send-direct-message-to-id
  :post
  "api.twitter.com/1/direct_messages/new.json"
  [:user-id
   :text]
  []
  (comp #(:content %) status-handler))

(def-twitter-method send-direct-message-to-name
  :post
  "api.twitter.com/1/direct_messages/new.json"
  [:screen-name
   :text]
  []
  (comp #(:content %) status-handler))

(def-twitter-method destroy-direct-message
  :post
  "api.twitter.com/1/direct_messages/destroy.json"
  [:id]
  []
  (comp #(:content %) status-handler))

(def-twitter-method create-friendship-to-id
  :post
  "api.twitter.com/1/friendships/create.json"
  [:user-id]
  [:follow]
  (comp #(:content %) status-handler))

(def-twitter-method create-friendship-to-name
  :post
  "api.twitter.com/1/friendships/create.json"
  [:screen-name]
  [:follow]
  (comp #(:content %) status-handler))

(def-twitter-method destroy-friendship-to-id
  :post
  "api.twitter.com/1/friendships/destroy.json"
  [:user-id]
  []
  (comp #(:content %) status-handler))

(def-twitter-method destroy-friendship-to-name
  :post
  "api.twitter.com/1/friendships/destroy.json"
  [:screen-name]
  []
  (comp #(:content %) status-handler))

(def-twitter-method show-friendship-by-ids
  :get
  "api.twitter.com/1/friendships/show.json"
  [:source-id
   :target-id]
  []
  (comp #(:content %) status-handler))

(def-twitter-method show-friendship-by-names
  :get
  "api.twitter.com/1/friendships/show.json"
  [:source-screen-name
   :target-screen-name]
  []
  (comp #(:content %) status-handler))

(def-twitter-method friends-of-id
  :get
  "api.twitter.com/1/friends/ids.json"
  [:user-id]
  []
  (comp #(:content %) status-handler))

(def-twitter-method friends-of-name
  :get
  "api.twitter.com/1/friends/ids.json"
  [:screen-name] 
  []
  (comp #(:content %) status-handler))

(def-twitter-method followers-of-id
  :get
  "api.twitter.com/1/followers/ids.json"
  [:user-id]
  []
  (comp #(:content %) status-handler))

(def-twitter-method followers-of-name
  :get
  "api.twitter.com/1/followers/ids.json"
  [:screen-name]
  []
  (comp #(:content %) status-handler))

(def-twitter-method verify-credentials
  :get
  "api.twitter.com/1/account/verify_credentials.json"
  []
  []
  (comp #(:content %) status-handler))

(def-twitter-method rate-limit-status
  :get
  "api.twitter.com/1/account/rate_limit_status.json"
  []
  []
  (comp #(:content %) status-handler))

(def-twitter-method end-session
  :post
  "api.twitter.com/1/account/end_session.json"
  []
  []
  (comp #(:content %) status-handler))

(def-twitter-method update-delivery-device
  :post
  "api.twitter.com/1/account/update_delivery_device.json"
  [:device]
  []
  (comp #(:content %) status-handler))

(def-twitter-method update-profile-colors
  :post
  "api.twitter.com/1/account/update_profile_colors.json"
  []
  [:profile-background-color
   :profile-text-color
   :profile-link-color
   :profile-sidebar-fill-color
   :profile-sidebar-border-color]
  (comp #(:content %) status-handler))


(comment (def-twitter-method update-profile-image
           :post
           "api.twitter.com/1/account/update_profile_image.json"
           [:image]
           []
           (comp #(:content %) status-handler)))

(defn update-profile-image [^String image]
  (let [req-uri__9408__auto__ "http://api.twitter.com/1/account/update_profile_image.json"
  
        oauth-creds__9414__auto__ (when
                                      (and
                                       *oauth-consumer*
                                       *oauth-access-token*)
                                    (oauth/credentials
                                     *oauth-consumer*
                                     *oauth-access-token*
                                     :post
                                     req-uri__9408__auto__))]
    ((comp #(:content %) status-handler)
     (http/post
      req-uri__9408__auto__
      :query
      oauth-creds__9414__auto__
      :parameters
      (http/map->params {:use-expect-continue false})
      :body (doto (MultipartEntity.)
              (.addPart "image" (FileBody. (File. image))))
      :as
      :json))))

(comment (def-twitter-method update-profile-background-image
           :post
           "api.twitter.com/1/account/update_profile_background_image.json"
           [:image]
           [:title]
           (comp #(:content %) status-handler)))

(defn update-profile-background-image [^String image & rest__2570__auto__]
  (let [req-uri__2571__auto__ "http://api.twitter.com/1/account/update_profile_background_image.json"
                              rest-map__2572__auto__ (apply hash-map rest__2570__auto__)
                              provided-optional-params__2573__auto__ (set/intersection
                                                                      (set [:title])
                                                                       (set
                                                                        (keys
                                                                         rest-map__2572__auto__)))
                              query-param-names__2574__auto__ (sort
                                                               (map
                                                                (fn 
                                                                 [x__2575__auto__]
                                                                 (keyword
                                                                  (string/replace
                                                                   (name
                                                                    x__2575__auto__)
                                                                   #"-"
                                                                   "_"
                                                                   )))
                                                                provided-optional-params__2573__auto__))
                              query-params__2576__auto__ (apply
                                                          hash-map
                                                           (interleave
                                                            query-param-names__2574__auto__
                                                            (vec
                                                             (vals
                                                              (sort
                                                               (select-keys
                                                                rest-map__2572__auto__
                                                                provided-optional-params__2573__auto__))))))
                              oauth-creds__2577__auto__ (when
                                                            (and
                                                             *oauth-consumer*
                                                             *oauth-access-token*)
                                                          (oauth/credentials
                                                           *oauth-consumer*
                                                           *oauth-access-token*
                                                           :post
                                                           req-uri__2571__auto__
                                                           query-params__2576__auto__))]
    ((comp #(:content %) status-handler)
     (http/post req-uri__2571__auto__
                :query (merge query-params__2576__auto__ oauth-creds__2577__auto__)
                :parameters (http/map->params {:use-expect-continue false})
                :body (doto (MultipartEntity.)
                        (.addPart "image" (FileBody. (File. image))))
                :as :json))))

(def-twitter-method update-profile
  :post
  "api.twitter.com/1/account/update_profile.json"
  []
  [:name 
   :email
   :url
   :location
   :description]
  (comp #(:content %) status-handler))

(def-twitter-method favorites
  :get
  "api.twitter.com/1/favorites.json"
  []
  [:id
   :page]
  (comp #(:content %) status-handler))

(def-twitter-method create-favorite
  :post
  "api.twitter.com/1/favorites/create.json"
  [:id]
  []
  (comp #(:content %) status-handler))

(def-twitter-method destroy-favorite
  :post
  "api.twitter.com/1/favorites/destroy.json"
  [:id]
  []
  (comp #(:content %) status-handler))

(def-twitter-method notifications-follow-by-id
  :post
  "api.twitter.com/1/notifications/follow.json"
  [:user-id]
  []
  (comp #(:content %) status-handler))

(def-twitter-method notifications-follow-by-name
  :post
  "api.twitter.com/1/notifications/follow.json"
  [:screen-name]
  []
  (comp #(:content %) status-handler))

(def-twitter-method notifications-leave-by-id
  :post
  "api.twitter.com/1/notifications/leave.json"
  [:user-id]
  []
  (comp #(:content %) status-handler))

(def-twitter-method notifications-leave-by-name
  :post
  "api.twitter.com/1/notifications/leave.json"
  [:screen-name]
  []
  (comp #(:content %) status-handler))

(def-twitter-method create-block
  :post
  "api.twitter.com/1/blocks/create.json"
  [:user-id-or-screen-name]
  []
  (comp #(:content %) status-handler))

(def-twitter-method destroy-block
  :post
  "api.twitter.com/1/blocks/destroy.json"
  [:user-id-or-screen-name]
  []
  (comp #(:content %) status-handler))

(def-twitter-method block-exists-for-id
  :get
  "api.twitter.com/1/blocks/exists.json"
  [:user-id]
  []
  (comp #(:content %) status-handler))

(def-twitter-method block-exists-for-name
  :get
  "api.twitter.com/1/blocks/exists.json"
  [:screen-name]
  []
  (comp #(:content %) status-handler))

(def-twitter-method blocking-users
  :get
  "api.twitter.com/1/blocks/blocking.json"
  []
  [:page]
  (comp #(:content %) status-handler))

(def-twitter-method blocking-user-ids
  :get
  "api.twitter.com/1/blocks/blocking/ids.json"
  []
  []
  (comp #(:content %) status-handler))

(def-twitter-method saved-searches
  :get
  "api.twitter.com/1/saved_searches.json"
  []
  []
  (comp #(:content %) status-handler))

(def-twitter-method show-saved-search
  :get
  "api.twitter.com/1/saved_searches/show.json"
  [:id]
  []
  (comp #(:content %) status-handler))

(def-twitter-method create-saved-search
  :post
  "api.twitter.com/1/saved_searches/create.json"
  [:query]
  []
  (comp #(:content %) status-handler))

(def-twitter-method destroy-saved-search
  :post
  "api.twitter.com/1/saved_searches/destroy.json"
  [:id]
  []
  (comp #(:content %) status-handler))

(def-twitter-method search
  :get
  "search.twitter.com/search.json"
  [:q]
  [:callback
   :lang
   :rpp
   :page
   :since-id
   :max-id
   :geocode
   :show-user]
  (comp #(:content %) status-handler))

(def-twitter-method trends
  :get
  "search.twitter.com/trends.json"
  []
  []
  (comp #(:content %) status-handler))

(def-twitter-method current-trends
  :get
  "search.twitter.com/trends/current.json"
  []
  [:exclude]
  (comp #(:content %) status-handler))

(def-twitter-method daily-trends
  :get
  "search.twitter.com/trends/daily.json"
  []
  [:date
   :exclude]
  (comp #(:content %) status-handler))

(def-twitter-method weekly-trends
  :get
  "search.twitter.com/trends/weekly.json"
  []
  [:date
   :exclude]
  (comp #(:content %) status-handler))

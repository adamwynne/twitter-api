(ns twitter.test.api.restful
  (:use
   [clojure.test]
   [twitter.test creds utils]
   [twitter.callbacks]
   [twitter.api.restful])
  (:import
   (java.io File)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(deftest test-account
  (is-200 verify-credentials)
  (is-200 rate-limit-status)
  (is-200 account-totals)
  (is-200 account-settings))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(deftest test-blocks
  (is (thrown? Exception (block-exists :oauth-creds (make-test-creds) :params {:screen-name "blahblah"})))
  (is-200 blocking-users)
  (is-200 blocking-user-ids))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(deftest test-timeline
  (is-200 public-timeline)
  (is-200 home-timeline)
  (is-200 user-timeline)
  (is-200 mentions)
  (is-200 retweeted-by-me)
  (is-200 retweeted-to-me)
  (is-200 retweets-of-me))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(deftest test-statuses
  (let [status-id (get-current-status-id *user-screen-name*)]
    (is-200 show-status :params {:id status-id})
    (is-200 show-retweets :params {:id status-id})
    (is-200 retweeted-by :params {:id status-id})
    (is-200 retweeted-by-ids :params {:id status-id})))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(deftest test-user
  (let [user-id (get-user-id *user-screen-name*)]
    (is-200 show-user :params {:user-id user-id})
    (is-200 lookup-users :params {:user-id user-id})
    (is-200 search-users :params {:q "john smith"})
    (is-200 suggest-slugs :params {:user-id user-id})
    (is-200 suggest-users-for-slug :params {:slug "sports"})
    (is-http-code 302 profile-image-for-user
                  :params {:screen-name *user-screen-name*}
                  :callbacks (callbacks-sync-single-debug))
    (is-200 show-contributees :params {:user-id user-id})))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(deftest test-trends
  (is-200 trends) 
  (is-200 current-trends) 
  (is-200 daily-trends)
  (is-200 weekly-trends))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(deftest test-local-trends
  (is-200 location-trends) 
  (is-200 current-trends :params {:woeid 1}))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defmacro with-list
  "create a list and then removes it"
  [list-id-name & body]
  
  `(with-setup-poll-teardown
     ~list-id-name
     (get-in (create-list :oauth-creds (make-test-creds) :params {:name "mytestlistblumblum"})
             [:body :id])
     (list-statuses :oauth-creds (make-test-creds) :params {:list-id ~list-id-name})
     (destroy-list :oauth-creds (make-test-creds) :params {:list-id ~list-id-name})
     ~@body))

(deftest test-lists
  (is-200 show-lists)
  (is-200 list-memberships)
  (is-200 list-subscriptions)
  
  (with-list list-id (is-200 list-statuses :params {:list-id list-id})))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(deftest test-list-members
  (with-list list-id
    (is-200 list-members :params {:list-id list-id})
    (is (thrown? Exception (check-member :params {:list-id list-id :screen-name *user-screen-name*})))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(deftest test-list-subscribers
  (with-list list-id
    (is-200 list-subscribers :params {:list-id list-id})
    (is (thrown? Exception (check-subscriber :params {:list-id list-id :screen-name *user-screen-name*})))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(deftest test-direct-messages
  (is-200 direct-messages)
  (is-200 sent-direct-messages))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(deftest test-friendship
  (is-200 show-friendship :params {:source-screen-name *user-screen-name* :target-screen-name "AdamJWynne"})
  (is-200 incoming-friendship)
  (is-200 outgoing-friendship))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(deftest test-friends-followers
  (is-200 show-friends)
  (is-200 show-followers))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(deftest test-favourites
  (let [status-id (get-current-status-id *user-screen-name*)]
    (is-200 create-favourite :params {:id status-id})
    (is-200 destroy-favourite :params {:id status-id})
    (is-200 favourites)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defmacro with-saved-search
  "create a saved search and then removes it"
  [search-id-name & body]
  
  `(with-setup-poll-teardown
     ~search-id-name
     (get-in (create-saved-search :oauth-creds (make-test-creds) :params {:query "sport"})
             [:body :id])
     (show-saved-search :oauth-creds (make-test-creds) :params {:id ~search-id-name})
     (destroy-saved-search :oauth-creds (make-test-creds) :params {:id ~search-id-name})
     ~@body))

(deftest test-saved-searches
  (is-200 saved-searches)
  (with-saved-search search-id (is-200 show-saved-search :params {:id search-id})))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

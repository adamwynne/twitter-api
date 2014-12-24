(ns twitter.api.test.restful
  (:use
   [clojure.test]
   [twitter.test-utils.core]
   [twitter.test creds utils]
   [twitter.callbacks]
   [twitter.api.restful])
  (:import
   (java.io File)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(deftest test-account
  (is-200 account-verify-credentials)
  (is-200 application-rate-limit-status)
  (is-200 application-rate-limit-status :app-only)
  (is-200 account-settings))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(deftest test-blocks
  (is-200 blocks-list)
  (is-200 blocks-ids))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(deftest test-timeline
  (is-200 statuses-mentions-timeline)
  (is-200 statuses-user-timeline)
  (is-200 statuses-home-timeline)
  (is-200 statuses-retweets-of-me))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(deftest test-statuses
  (let [status-id (get-current-status-id *user-screen-name*)]
    (is-200 statuses-show-id :params {:id status-id})
    (is-200 statuses-show-id :params {:id status-id} :app-only)
    (is-200 statuses-retweets-id :params {:id status-id})
    (is-200 statuses-retweets-id :params {:id status-id}) :app-only))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(deftest test-search
  (is-200 search-tweets :params {:q "clojure"})
  (is-200 search-tweets :params {:q "clojure"} :app-only))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(deftest test-user
  (let [user-id (get-user-id *user-screen-name*)]
    (is-200 users-show :params {:user-id user-id})
    (is-200 users-show :params {:user-id user-id} :app-only)
    (is-200 users-lookup :params {:user-id user-id})
    (is-200 users-lookup :params {:user-id user-id} :app-only)
    (is-200 users-suggestions :params {:q "john smith"})
    (is-200 users-suggestions :params {:q "john smith"} :app-only)
    (is-200 users-suggestions-slug :params {:slug "sports"})
    (is-200 users-suggestions-slug-members :params {:slug "sports"})
    ;; The following test seems to be broken as of 23/12/14
    ;;(is-200 users-contributees :params {:user-id user-id})
))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(deftest test-trends
  (is-200 trends-place :params {:id 1})
  (is-200 trends-place :params {:id 1} :app-only)
  (is-200 trends-available)
  (is-200 trends-available :app-only)
  (is-200 trends-closest :params {:lat 37.781157 :long -122.400612831116})
  (is-200 trends-closest :params {:lat 37.781157 :long -122.400612831116} :app-only))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defmacro with-list
  "create a list and then removes it"
  [list-id-name & body]
  
  `(with-setup-poll-teardown
     ~list-id-name
     (get-in (lists-create :oauth-creds (make-test-creds) :params {:name "mytestlistblumblum"})
             [:body :id])
     (lists-statuses :oauth-creds (make-test-creds) :params {:list-id ~list-id-name})
     (lists-destroy :oauth-creds (make-test-creds) :params {:list-id ~list-id-name})
     ~@body))

(deftest test-lists
  (is-200 lists-list)
  (is-200 lists-memberships)
  (is-200 lists-subscriptions)
  (is-200 lists-ownerships)

  (with-list list-id (is-200 lists-statuses :params {:list-id list-id})))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(deftest test-list-members
  (with-list list-id
    (is-200 lists-members :params {:list-id list-id})
    (is (thrown? Exception (lists-members-show :params {:list-id list-id :screen-name *user-screen-name*})))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(deftest test-list-subscribers
  (with-list list-id
    (is-200 lists-subscribers :params {:list-id list-id})
    (is (thrown? Exception (lists-subscribers-show :params {:list-id list-id :screen-name *user-screen-name*})))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(deftest test-direct-messages
  (is-200 direct-messages)
  (is-200 direct-messages-sent))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(deftest test-friendship
  (is-200 friendships-show :params {:source-screen-name *user-screen-name* :target-screen-name "AdamJWynne"})
  (is-200 friendships-show :params {:source-screen-name *user-screen-name* :target-screen-name "AdamJWynne"} :app-only)
  (is-200 friendships-lookup :params { :screen-name "peat,AdamJWynne" } )
  (is-200 friendships-incoming)
  (is-200 friendships-outgoing))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(deftest test-friends-followers
  (is-200 friends-ids)
  (is-200 friends-list)
  (is-200 followers-ids)
  (is-200 followers-list))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(deftest test-favourites
  (let [status-id (get-current-status-id *user-screen-name*)]
    (is-200 favorites-create :params {:id status-id})
    (is-200 favorites-destroy :params {:id status-id})
    (is-200 favorites-list)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defmacro with-saved-search
  "create a saved search and then removes it"
  [search-id-name & body]
  `(with-setup-poll-teardown
     ~search-id-name
     (get-in (saved-searches-create :oauth-creds (make-test-creds) :params {:query "sandwiches"})
             [:body :id])
     (saved-searches-show-id :oauth-creds (make-test-creds) :params {:id ~search-id-name})
     (saved-searches-destroy-id :oauth-creds (make-test-creds) :params {:id ~search-id-name})
     ~@body))

(deftest test-saved-searches
  (is-200 saved-searches-list)
  (with-saved-search search-id (is-200 saved-searches-show-id :params {:id search-id})))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

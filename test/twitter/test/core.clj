(ns twitter.test.core
  (:use
   [clojure.test]
   [twitter.test.creds]
   [twitter oauth api utils core callbacks])
  (:import
   (twitter.api ApiContext)))

(deftest test-sign-query
  (let [result (sign-query (make-test-creds) :get "http://www.cnn.com" :query {:test-param "true"})]
    (is (:oauth_signature result))))
  
(deftest test-oauth-header-string
  (is (= (oauth-header-string {:a 1 :b 2 :c 3}) "OAuth c=\"3\",b=\"2\",a=\"1\""))
  (is (= (oauth-header-string {:a "hi there"}) "OAuth a=\"hi%20there\""))
  (is (= (oauth-header-string {:a "hi there"} :url-encode? nil) "OAuth a=\"hi there\""))
  (is (= (oauth-header-string {:bearer "hello"}) "Bearer hello")))

(deftest test-make-uri
  (is (= (make-uri (ApiContext. "http" "api.twitter.com" 1) "users/show.json") "http://api.twitter.com/1/users/show.json"))
  (is (= (make-uri (ApiContext. "http" "api.twitter.com" nil) "users/show.json") "http://api.twitter.com/users/show.json")))

(deftest test-sub-uri
  (is (= (subs-uri "http://www.cnn.com/{:version}/{:id}/test.json" {:version 1, :id "my123"})
         "http://www.cnn.com/1/my123/test.json"))
  (is (= (subs-uri "http://www.cnn.com/nosubs.json" {:version 1, :id "my123"})
         "http://www.cnn.com/nosubs.json"))
  (is (thrown? Exception (subs-uri "http://www.cnn.com/{:version}/{:id}/test.json" {:id "my123"}))))

(deftest test-fix-keyword
  (is (= (#'twitter.core/fix-keyword :my-test) :my_test)))

(deftest test-fix-colls
  (is (= (#'twitter.core/fix-colls :a) :a))
  (is (= (#'twitter.core/fix-colls [1 2 3]) "1,2,3")))

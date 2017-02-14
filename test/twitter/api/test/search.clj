(ns twitter.api.test.search
  (:use
   [clojure.test]
   [twitter.test-utils.core]
   [twitter.test.creds]
   [twitter.test.utils]
   [twitter.callbacks]
   [twitter.api.search]))

(deftest test-search
  (is-200 search :params {:q "sports"}))

(ns twitter.api.test.search
  (:require [clojure.test :refer :all]
            [twitter.api.search :refer [search]]
            [twitter.test-utils.core :refer [is-200]]))

(deftest test-search
  (is-200 search :params {:q "sports"}))

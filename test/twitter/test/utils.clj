(ns twitter.test.utils
  (:require [clojure.test :refer :all]
            [twitter.utils :refer [get-file-ext
                                   transform-map]]))

(deftest test-transform-map
  (is (= (transform-map {:a 0 :b 1 :c 2 :d 3} :key-trans name) {"a" 0, "b" 1, "c" 2, "d" 3}))
  (is (= (transform-map {:a 0 :b 1 :c 2 :d 3} :val-trans inc) {:a 1 :b 2 :c 3 :d 4})))

(deftest test-get-file-ext
  (is (= (get-file-ext "adam.ext") "ext"))
  (is (nil? (get-file-ext "adam")))
  (is (= (get-file-ext "adam/was/here.ext") "ext"))
  (is (nil? (get-file-ext "adam/was/here.ext."))))

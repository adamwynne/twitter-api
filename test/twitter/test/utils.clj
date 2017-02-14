(ns twitter.test.utils
  (:use [clojure.test]
        [twitter.utils]))

(deftest test-transform-map
  (is (= (transform-map {:a 0 :b 1 :c 2 :d 3} :key-trans name) {"a" 0, "b" 1, "c" 2, "d" 3}))
  (is (= (transform-map {:a 0 :b 1 :c 2 :d 3} :val-trans inc) {:a 1 :b 2 :c 3 :d 4})))

(deftest test-partition-map
  (is (= (partition-map {:a 1 :b 2 :c 3 :d 4} (comp even? second)) [{:d 4, :b 2} {:c 3, :a 1}]))
  (is (= (partition-map {:a 1 :b 2 :c 3 :d 4} (comp (partial < 0) second)) [{:d 4, :c 3, :b 2, :a 1} {}]))
  (is (= (partition-map {} even?) [{} {}])))

(deftest test-get-file-ext
  (is (= (get-file-ext "adam.ext") "ext"))
  (is (nil? (get-file-ext "adam")))
  (is (= (get-file-ext "adam/was/here.ext") "ext"))
  (is (nil? (get-file-ext "adam/was/here.ext."))))

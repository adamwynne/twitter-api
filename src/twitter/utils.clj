(ns twitter.utils
  (:use
   [clojure.test]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn transform-map
  "transforms the k/v pairs of a map using a supplied transformation function"
  [map-to-transform & {:keys [key-trans val-trans] :or {key-trans identity val-trans identity}}]

  (if map-to-transform
    (into {} (map (fn [[k v]] [(key-trans k) (val-trans v)]) map-to-transform))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(deftest test-transform-map
  (is (= (transform-map {:a 0 :b 1 :c 2 :d 3} :key-trans name) {"a" 0, "b" 1, "c" 2, "d" 3}))
  (is (= (transform-map {:a 0 :b 1 :c 2 :d 3} :val-trans inc) {:a 1 :b 2 :c 3 :d 4})))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn partition-map
  "partitions a map, depending on a predicate, returning a vector of maps of passes and fails"
  [map-to-partition pred]
  
  (loop [passes {}
         fails {}
         m map-to-partition]
    (if (empty? m) [passes fails]
        (let [[k v] (first m)]
          (if (pred [k v])
            (recur (assoc passes k v) fails (rest m))
            (recur passes (assoc fails k v) (rest m)))))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(deftest test-partition-map
  (is (= (partition-map {:a 1 :b 2 :c 3 :d 4} (comp even? second)) [{:d 4, :b 2} {:c 3, :a 1}]))
  (is (= (partition-map {:a 1 :b 2 :c 3 :d 4} (comp (partial < 0) second)) [{:d 4, :c 3, :b 2, :a 1} {}]))
  (is (= (partition-map {} even?) [{} {}])))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(ns nightshade.alg
  (:require [clojure.core.reducers :as r]))

(defn fib-lazy
  [n]
  (->> [0 1]
       (map bigint)
       (iterate (fn [[a b]] [b (+ a b)]))
       (r/map first)
       (r/take (inc n))))

(defn fib-lazy-seq
  ([] (fib-lazy-seq [0N 1N]))
  ([[a b]]
   (cons [a b] (lazy-seq (fib-lazy-seq [b (+ a b)])))))

(defn map-by-fold
  [f colls]
  (r/fold
   r/cat
   #(r/append! %1 (f %2))
   (vec colls)))

(defn my-iterate
  [fn val]
  (cons val (lazy-seq (my-iterate fn (fn val)))))

(defn odds-even
  ([colls]
   (odds-even colls [0 0]))
  ([colls [odds evens]]
   (if (empty? colls)
     [odds evens]
     (let [fst (first colls)]
       (if (odd? fst)
         (recur (rest colls) [(inc odds) evens])
         (recur (rest colls) [odds (inc evens)]))))))

(defn- odd-or-even
  [[o e :as oe] x]
  (if (odd? x)
      [(inc o) e]
      [o (inc e)]))

(defn odds-even-lazy
  ([[first & rest-elems] [o e :as oe]]
   (let [res (odd-or-even oe first)]
     (cons res (lazy-seq (odds-even-lazy rest-elems res)))))
  ([colls]
   (odds-even-lazy colls [0 0])))

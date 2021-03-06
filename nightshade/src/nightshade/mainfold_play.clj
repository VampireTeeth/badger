(ns nightshade.mainfold-play
  (:require [manifold.deferred :as d]
            [manifold.stream :as s]))


(defn basic-put-take
  []
  (let [s (s/stream)]
    (d/chain (s/put! s :food)
             #(prn :deliverred %))
    (Thread/sleep 1000)
    @(s/take! s)))

(defn basic-try-put-take
  [put-timeout take-delay]
  (let [s (s/stream)]
    (d/chain (s/try-put! s :foo put-timeout ::timeout)
             #(prn 'deliverred? %))
    (Thread/sleep take-delay)
    (d/chain (s/take! s) #(prn 'message! %))))

(defn basic-try-take-put
  [take-timeout put-delay]
  (let [s (s/stream)]
    (d/chain (s/try-take! s ::drained take-timeout ::take-timeout)
             #(prn 'message! %))
    (Thread/sleep put-delay)
    (d/chain (s/put! s :bar) #(prn 'deliverred? %))))

(defn- take-from-stream
  [s timeout]
  (d/chain (s/try-take! s ::drained timeout ::take-timeout)
           (fn [m]
             (prn 'message! m)
             m)))

(defn timeout-and-drained-take
  []
  (let [s (s/stream)]
    (d/chain (take-from-stream s 1000)
             (fn [_] (s/close! s) s)
             (fn [s]
               (prn 'closed! s)
               (take-from-stream s 1000)))))

(defn derived-stream
  []
  (let [s (s/stream)
        a (s/map inc s)
        b (s/map dec s)]
    (d/chain (s/put! s 0) #(prn :taken? %))
    (d/chain (s/take! a) #(prn :inc %))
    (d/chain (s/take! b) #(prn :dec %))))

(defn connecting-streams
  []
  (let [a (s/stream)
        b (s/stream)]
    (s/connect a b)
    (d/chain (s/put! a 0) #(prn :taken? %))
    (d/chain (s/take! b) #(prn :received %))))

(ns core-async-csp.core
  (:gen-class)
  (:require [clojure.core.async :as async
             :refer [>!! >! <!! <! alts!! alts!! alt! alt!! chan close! go thread timeout]]))

(defn- current-thread-name
  []
  (-> (Thread/currentThread)
      .getName))

(defn chan-put-take
  []
  (let [c (chan)]
    (thread
      (Thread/sleep 1000)
      (>!! c "hello")
      (println (current-thread-name) "Put done"))
    (println (current-thread-name) "Got:" (<!! c))
    (close! c)))

(defn chan-go-put-take
  []
  (let [c (chan)]
    (go
      (Thread/sleep 1000)
      (>! c "hello")
      (println (current-thread-name) "Put done"))
    (println (current-thread-name) "Got:" (<!! c))
    (close! c)))

(defn chan-go-put-go-take
  []
  (let [c (chan)]
    (go
      (Thread/sleep 1000)
      (>! c "hello")
      (println (current-thread-name) "Put done"))
    (<!! (go
           (println (current-thread-name) "Got:" (<! c))
           "done"))))

(defn alts!!-read-2-chan
  []
  (let [c1 (chan)
        c2 (chan)]
    (thread
      (let [[v ch] (alts!! [c1 c2])]
        (Thread/sleep 1000)
        (println (current-thread-name) "Read" v "from" ch)))
    (go (>! c1 "hi"))
    (go (>! c2 "there"))
    "done"))

(defn- random-sleep
  [max-secs]
  (let [millis (-> 5 rand-int (* 1000))]
    (Thread/sleep millis)))

(defn- put-with-random-sleep
  [ch v secs]
  (go
    (random-sleep secs)
    (>! ch v)))

(defn- take-with-random-sleep
  [ch secs]
  (go
    (random-sleep secs)
    (<! ch)))

(defn alt!!-wait-ops-on-chans
  []
  (let [c1 (chan)
        c2 (chan)
        c3 (chan)
        c4 (chan)]

    ;;(put-with-random-sleep c1 "hello" 10)
    ;;(put-with-random-sleep c2 "world" 8)
    (go (<! (timeout 3000)) (>! c1 "hello"))
    (go (<! (timeout 6000)) (>! c2 "world"))

    (go (<! (timeout 4000)) (println (current-thread-name) "Got" (<! c3)))
    (go (<! (timeout 4500)) (println (current-thread-name) "Got" (<! c4)))

    (<!!
     (thread
       (alt!!
         ;; Wait for the first take operation on c1 and c2
         [c1 c2]
         ([v ch]
          (println (current-thread-name) "Got" v)
          [v ch])

         ;; Wait for the first put operation on c3 and c4
         [[c3 "hi"] [c4 "there"]]
         ([_ ch] [:put-done ch]))))
    (doseq [c [c1 c2 c3 c4]]
      (close! c))))

(defn channel-timeout
  []
  (let [c (chan)
        t (timeout 3000)]
    (go
      (<! (timeout 4000))
      (>! c "hello"))
    (let [[v ch] (alts!! [c t])]
      (println "Got" v))
    (close! c)))

(defn channel-timeout-with-keyword
  []
  (let [c (chan)
        t (timeout 5000)]
    (try
      (go
        (<! (timeout 6000))
        (>! c "hello"))
      (let [v
            (alt!!
              t :timeout
              c ([v _] v))]
        (println "Got" v))
      (finally
        (close! c)))))

(defn -main
  "i don't do a whole lot ... yet."
  [& args]
  (println "hello, world!"))

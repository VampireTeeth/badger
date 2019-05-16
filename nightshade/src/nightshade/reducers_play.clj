(ns nightshade.reducers-play
  (:require [clojure.core.reducers :as r]
            [clojure.string :as s]))


(defn- count-occurences
  [words]
  (r/fold
   (r/monoid #(merge-with + %1 %2) (constantly {}))
   (fn [m w] (assoc m w (-> (get m w 0) inc)))
   words))

(defn get-words-count
  "Example uri: http://www.gutenberg.org/files/1399/1399-0.txt"
  [uri]
  (count (->
          (slurp uri)
          (s/split #"\s+")
          count-occurences)))

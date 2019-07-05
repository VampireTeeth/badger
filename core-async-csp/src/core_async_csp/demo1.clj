(ns core-async-csp.demo1
  (:gen-class)
  (:require [clojure.core.async :as async
             :refer [>!! >! <!! <! alts!! alts!! alt! alt!! chan close! go thread timeout go-loop]]))

(defn render
  [q]
  (apply
   str
   (for [p (reverse q)]
     (str "<div class='proc-" p "'>Process " p "</div>"))))

(defn peekn
  "Returns a sub-vector containing (up to) n items from the original vector"
  [v n]
  (if (> (count v) n)
    (subvec v (- (count v) n))
    v))

(defn -main
  "First CSP demo"
  [& args]
  (let [c (chan)]
    (go (while true (<! (timeout 1000)) (>! c 1)))
    (go (while true (<! (timeout 2000)) (>! c 2)))
    (go (while true (<! (timeout 3000)) (>! c 3)))

    (<!! (go-loop [q []]
      (println (render q))
      (recur (-> (conj q (<! c)) (peekn 10)))))))

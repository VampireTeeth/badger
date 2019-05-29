(ns nightshade.score-climbing)

(defn- make-rank-scores
  ([scores]
   (make-rank-scores scores [[0, Integer/MAX_VALUE]]))

  ([[h & t] res]
     (if (nil? h)
       res
       (let [[r s] (last res)
             new-rank (if (= s h) r (inc r))]
         (recur t (conj res [new-rank h]))))))

(defn score-climbing
  [scores alice]
  (let [inner
        (fn inner [[[rs-rank rs-score :as rs-head] & rs-tail :as rank-scores]
                   [head & tail :as alice]
                   output]
          ;;(println output)
          (if (nil? head)
            output
            (if (nil? rs-head)
              (recur rank-scores tail (conj output [1 head]))
              (if (> head rs-score)
                (recur rs-tail alice output)
                (recur rank-scores tail (conj output [(if (= rs-score head)
                                                    rs-rank
                                                    (inc rs-rank)) head]))))))]

    (map first (-> (reverse (make-rank-scores scores)) (inner alice [])))))

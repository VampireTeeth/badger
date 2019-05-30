(ns nightshade.longest-increase-subsequence)

(defn- find-cur-LIS
  "Takes a vector of LISs ending at previous indexes
  and returns the LIS for current value"
  ([prev val]
   (find-cur-LIS prev val [val]))
  ([[head & tail :as prev] val out]
   (if (nil? head)
    out
    (if (< (last head) val)
      (let [cand (conj head val)
            cand-count (count cand)
            out-count (count out)]
        (if (>= cand-count out-count)
          (recur tail val cand)
          (recur tail val out)))
      (recur tail val out)))))

(defn find-LIS-DP
  [values]
  (loop [[cur-val & rest-vals] values
         out ['() 1]]
    (if (nil? cur-val)
      out
      (let [old-LIS-seq (first out)
            old-cnt (second out)
            new-LIS (find-cur-LIS (vec old-LIS-seq) cur-val)
            new-LIS-seq (cons new-LIS old-LIS-seq)
            new-cnt-cand (count new-LIS)
            new-cnt (max old-cnt new-cnt-cand)]
        ;;(println "new-LIS-seq" new-LIS-seq)
        ;;(println out)
        (recur rest-vals [new-LIS-seq new-cnt])))))

(defn find-LIS-count
  [values]
  (-> (find-LIS-DP values) second))

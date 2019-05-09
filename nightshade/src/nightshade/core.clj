(ns nightshade.core
  (:require [manifold.deferred :as d])
  (:require [aleph.http :as http])
  (:require [byte-streams :as bs])
  (:gen-class))


(def d (d/deferred))

(d/chain d
         #(future (inc %))
         #(println "The future retutned" %))



(def errd (d/deferred))

(-> errd
    (d/chain dec #(/ 1 %))
    (d/catch Exception #(println "Whoops, that did not work!" %)))


(when (not (resolve 'web-server))
        (def web-server))

(defn- my-handler
  [req]
  {:status 200
   :headers {"content-type" "text/plain"}
   :body "hello!"})

(defn- start-server
  [_]
  (http/start-server
   my-handler
   {:port 3000}))

(defn go
  []
  (when (bound? #'web-server)
    (.close web-server))
  (alter-var-root #'web-server start-server))



;; Client code
(defn hello-web-client
  []
  (-> (http/get "http://localhost:3000")
      (d/chain :body
               bs/to-string
               println)))

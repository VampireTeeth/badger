(ns nightshade.core
  (:require [manifold.deferred :as d]
            [aleph.http :as http]
            [bidi.bidi :as bidi]
            [bidi.ring :refer [make-handler]]
            [ring.util.response :as res]
            [ring.middleware.json :refer [wrap-json-response]]
            [byte-streams :as bs])
  (:gen-class))


(def d (d/deferred))

(d/chain d
         #(future (inc %))
         #(println "The future retutned" %))



(def errd (d/deferred))

(-> errd
    (d/chain dec #(/ 1 %) println)
    (d/catch Exception #(println "Whoops, that did not work!" %)))

(defn- my-handler
  [req]
  {:status 200
   :headers {"content-type" "text/plain"}
   :body "hello!"})


;; Bidi playing
(defn- list-users
  [req]
  (res/response {:message "list-users"}))

(defn- account-by-id
  [req]
  (println req)
  (res/response {:message (str "account-by-id for " (-> req :params :id))}))

(defn- list-accounts
  [req]
  (res/response {:message "list-accounts"}))

(def my-routes
  ["/" {"users" list-users
        "accounts"
        {["/" :id] account-by-id
         "" list-accounts}}])


;; web-server initialization
(when (not (resolve 'web-server))
        (def web-server))

(defn- start-server
  [_]
  (http/start-server
   ;;my-handler
   (-> my-routes make-handler wrap-json-response)
   {:port 3000}))


(defn go
  []
  (when (bound? #'web-server)
    (.close web-server))
  (alter-var-root #'web-server start-server))


;; Client code
(def server-base-url "http://localhost:3000")

(defn get-users
  []
  (-> (http/get (str server-base-url "/users"))
      (d/chain :body
               bs/to-string
               println)))


(defn get-accounts
  []
  (-> (http/get (str server-base-url "/accounts"))
      (d/chain :body
               bs/to-string
               println)))


(defn get-account-by-id
  [id]
  (-> (http/get (str server-base-url "/accounts/" id))
      (d/chain :body
               bs/to-string
               println)))

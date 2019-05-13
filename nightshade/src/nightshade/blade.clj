(ns nightshade.blade
  (:require [manifold.deferred :as d]
            [aleph.http :as http]
            [bidi.bidi :as bidi]
            [bidi.ring :refer [make-handler]]
            [liberator.core :refer [defresource]]
            [liberator.dev :refer [wrap-trace]]
            [cemerick.url :as url]
            [nightshade.dev :as dev]
            [byte-streams :as bs]))

(defresource hello-world
  :available-media-types ["text/plain"]
  :handle-ok "Hello, world!")

(defresource hello-to
  :available-media-types ["text/plain" "application/json"]
  :exists?
  (fn [ctx]
    {::name (get-in ctx [:request :route-params :name])})
  :handle-ok
  (fn [ctx]
    (let [media-type (get-in ctx [:representation :media-type])]
      (condp = media-type
        "text/plain" (str "Hello," (::name ctx))
        "application/json" {:hello (::name ctx)})))

  :handle-not-acceptable
  (fn [ctx]
    "I dont understand what you say"))


(defresource timehop
  :available-media-types ["text/plain"]
  ;; Time hops every 60 seconds
  :last-modified (-> (System/currentTimeMillis) (/ 60000) long (* 60000))
  :handle-ok
  (fn [_]
    (format "It is now %s" (java.util.Date.))))

(defresource changetag
  :available-media-types ["text/plain"]
  :etag
  (let [i (-> (System/currentTimeMillis) (/ 60000) (mod 10) int)]
    (-> "abcdefghijklmnopqrstuvwxyz" (.substring i (+ i 10)))))

(def users-count (ref 0))

(defresource user-resource
  :available-media-types ["application/json" "application/xml"]
  :allowed-methods [:get :post]
  :handle-ok
  (fn [ctx]
    (println "handle-ok")
    {:status 200 :count @users-count})

  :post!
  (fn [ctx]
    (dosync
     (println "post!")
     (alter users-count inc)
     {::count @users-count}))

  :post-redirect?
  (fn [ctx] {:location "/user"}))

(def blade-routes
  ["/" {"hello" {"" hello-world
                 ["/" :name] hello-to}

        "timehop" timehop

        "changetag" changetag

        "user" user-resource}])

(defn go
  []
  (dev/go
    (-> blade-routes make-handler (wrap-trace :header :ui))))


;; Client code
(def base-url "http://localhost:3000")

(defn get-hello
  []
  (->
   (str base-url "/hello")
   http/get
   (d/chain :body bs/to-string println)))


(defn get-hello-to
  [name]
  (let [resource-url (str base-url "/hello/" (url/url-encode name))]
    (->
     resource-url
     (http/get {:headers {:accept "application/xml"}})
     (d/chain :body bs/to-string println)
     (d/catch Exception #(-> % .getData :body bs/to-string println)))))

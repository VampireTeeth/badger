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

(def blade-routes
  ["/" {"hello"
        {"" hello-world
         ["/" :name] hello-to}}])

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

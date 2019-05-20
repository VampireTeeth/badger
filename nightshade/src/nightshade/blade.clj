(ns nightshade.blade
  (:require [manifold.deferred :as d]
            [manifold.stream :as s]
            [aleph.http :as http]
            [bidi.bidi :as bidi]
            [bidi.ring :refer [make-handler]]
            [liberator.core :refer [defresource]]
            [liberator.dev :refer [wrap-trace]]
            [liberator.representation :refer [ring-response as-response]]
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

(defresource custom-response
  :available-media-types ["application/json"]
  :allowed-methods [:get]

  :handle-ok
  (fn [ctx]
    (ring-response {:some "Json"}
                   {:status 202
                    :headers {"X-Foo-Header" "This is me"}})))

(defresource not-found
  :available-media-types ["application/json"]
  :exists? nil
  :handle-not-found
  (fn [ctx]
    {:message "Resource not found"}))

(defresource streamed-number
  :available-media-types ["text/plain"]
  :allowed-methods [:get]

  :exists?
  (fn [ctx]
    {::cnt (Integer/parseInt (get-in ctx [:request :route-params :cnt]))})

  :handle-ok
  (fn [ctx]
    (let [sent (atom 0)
          cnt (::cnt ctx)]
      (->>
       (s/periodically 100 #(str (swap! sent inc) "\n"))
       (s/transform (take cnt))))))

(extend-protocol liberator.representation/Representation
  manifold.stream.SourceProxy
  (as-response [r ctx]
    (assoc ctx :body r)))

(def blade-routes
  ["/" {"hello" {"" hello-world
                 ["/" :name] hello-to}

        "timehop" timehop

        "changetag" changetag

        "user" user-resource

        "custom" custom-response

        ["numbers/" :cnt] streamed-number

        true not-found}])


;; Middleware for wrapping the response into
;; a manifold.deferred
(defn wrap-deferred
  [handler]
  (fn [req]
    (d/future (handler req))))

(defn go
  []
  (dev/go
    (-> blade-routes
        make-handler
        (wrap-trace :header :ui)
        wrap-deferred)))


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

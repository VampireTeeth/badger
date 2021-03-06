(ns sandbox.core
  (:require [yada.yada :as yada]
            [clojure.data.json :as json])
  (:gen-class))

;; Cookies definitions
(def my-cookie
  {:session
   {:name "session"
    :max-age 3600
    :domain "vampireteeth.com"
    :path "/"
    :secure true
    :http-only true}})

;; Response handler functions
(defn- custom-cookie
  [ctx]
  ;;(println ctx)
  (yada/set-cookie
   ctx :session
   "12345"))

(defn- hello-to
  [ctx]
  (let [name (-> ctx :parameters :path :name)]
    {:message (str "Hello," name)}))

(defn- hello
  [ctx]
  (update-in ctx [:response :body]
             (fn [_] (json/json-str {:message "Hello"}))


;; Definitions of resource
(def default-404
  {:produces
   {:media-type "application/json" :charset "utf-8"}
   :response
   {:status 404 :message "Not Found"}})

(def route-no-match-resource
  {:methods
   {:* nil}
   :properties {:exists? false}
   :responses
   {404 default-404}})

(def custom-cookie-resource
  {:cookies
   my-cookie
   :methods
   {:get
    {:produces {:media-type "application/json" :charset "UTF-8"}
     :response (comp hello custom-cookie)}}})

(def hello-to-resource
  (yada/resource
   {:methods
    {:get
     {:produces
      [{:media-type "application/json" :charset "UTF-8"}
       {:media-type "application/json;charset=utf-8"}]
      :response hello-to}}}))

;; Define a new variable web-server to hold the web-server
;; created via yada/listener
(when (not (resolve 'web-server))
  (def web-server))

(defn- make-web-server
  []
  (yada/listener
   ["/"
    [
     ["hello" (yada/as-resource "Hello World!")]
     [["hello/" :name] hello-to-resource]
     ["test" (yada/resource {:produces "text/plain"
                             :response "This is a test!"})]
     ["my-cookie" (yada/resource custom-cookie-resource)]
     [true (yada/resource route-no-match-resource)]]]

   {:port 3000}))

(defn- new-web-server
  [_]
  ;; Creating a new web server
  (make-web-server))

(defn stop
  []
  (when (bound? #'web-server)
    ((:close web-server))))

(defn go
  []
  (when (and (resolve 'web-server) (bound? #'web-server))
    ((:close web-server)))
  (alter-var-root #'web-server new-web-server))

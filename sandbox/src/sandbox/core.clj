(ns sandbox.core
  (:require [yada.yada :as yada])
  (:gen-class))

(defn- hello-to
  [name]
  {:message (str "Hello," name)})

;; Definitions of resource
(def default-404
  {:produces
   {:media-type "application/json" :charset "utf-8"}
   :response
   {:status 404 :message "Not Found"}})

(def route-no-match-resource
  {:methods
   {:* default-404}
   :properties {:exists? false}
   :responses
   {404 default-404}})


(def hello-to-resource
  (yada/resource
   {:methods
    {:get
     {:produces
      {:media-type "application/json" :charset "utf-8"}
      :response (fn [ctx]
                  (-> ctx :parameters :path :name hello-to))}}}))
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

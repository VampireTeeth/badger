(ns sandbox.core
  (:require [yada.yada :as yada])
  (:gen-class))

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
     ["test" (yada/resource {:produces "text/plain"
                             :response "This is a test!"})]
     [true (yada/as-resource nil)]]]
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

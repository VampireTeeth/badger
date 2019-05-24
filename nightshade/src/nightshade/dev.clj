(ns nightshade.dev
  (:require [aleph.http :as http]
            [nightshade.blade :as blade]))


;; web-server initialization
(when (not (resolve 'web-server))
        (def web-server))

(defn- start-server
  [handler _]
  (http/start-server
   ;;my-handler
   handler
   {:port 3000}))


(defn go
  [handler]
  (when (bound? #'web-server)
    (.close web-server))
  (alter-var-root #'web-server (partial start-server handler)))

(defn blade-go
  []
  (go (blade/blade-handler)))

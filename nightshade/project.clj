(defproject nightshade "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [ring/ring-core "1.7.1"]
                 [ring/ring-json "0.4.0"]
                 [liberator "0.15.3"]
                 [bidi "2.1.6"]
                 [manifold "0.1.8"]
                 [aleph "0.4.6"]
                 [com.cemerick/url "0.1.1"]]
  :main ^:skip-aot nightshade.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})

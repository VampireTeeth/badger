(defproject sandbox "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.0"]
                 ;;[yada "1.2.15"]
                 [org.clojure/data.json "0.2.6"]
                 [yada "1.3.0-alpha9"]
                 [bidi "2.1.6"]
                 [byte-streams "0.2.4"]]
  :main ^:skip-aot sandbox.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})

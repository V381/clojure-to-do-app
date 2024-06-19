(defproject your-app-name "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [compojure "1.6.2"]
                 [ring/ring-core "1.9.3"]
                 [ring/ring-jetty-adapter "1.9.3"]
                 [ring/ring-defaults "0.3.2"]
                 [hiccup "1.0.5"]
                 [cheshire "5.10.0"]]
  :main ^:skip-aot your-app-name.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})

(defproject your-app-name "0.1.0-SNAPSHOT"
  :description "Clojure ToDo by Pavle"
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [compojure "1.6.2"]
                 [ring/ring-defaults "0.3.2"]
                 [hiccup "1.0.5"]
                 [cheshire "5.10.0"]]
  :min-lein-version "2.0.0"
  :source-paths ["src"]
  :main ^:skip-aot your-app-name.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})

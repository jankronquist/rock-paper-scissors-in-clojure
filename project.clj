(defproject rock-paper-scissors "1.0.0-SNAPSHOT"
  :description "The game rock-paper-scissors implemented using CQRS & Event Sourcing in Clojure"
  :dependencies [[org.clojure/clojure "1.5.0"]
                 [clj-http "0.7.7"]
                 [compojure "1.1.6"]
                 [environ "0.4.0"]
                 [hiccup "1.0.4"]]
  :main com.jayway.rps.main
  :plugins [[lein-ring "0.8.8"]]
  :min-lein-version "2.0.0"
  :ring {:handler com.jayway.rps.web/app}
  :profiles
  {:dev {:dependencies [[javax.servlet/servlet-api "2.5"]
                        [ring-mock "0.1.5"]]}})
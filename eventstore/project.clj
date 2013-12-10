(defproject rock-paper-scissors-eventstore "1.0.0-SNAPSHOT"
  :description "The game rock-paper-scissors implemented using CQRS & Event Sourcing in Clojure"
  :dependencies [[org.clojure/clojure "1.5.0"]
                 [rock-paper-scissors-core "1.0.0-SNAPSHOT"]]
  :main com.jayway.rps.main
  :plugins [[lein-ring "0.8.8"]]
  :min-lein-version "2.0.0"
  :ring {:handler com.jayway.rps.eventstore.web/app}
  :profiles {:dev {:dependencies [[javax.servlet/servlet-api "2.5"]
                                  [ring-mock "0.1.5"]]}})
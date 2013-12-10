(defproject rock-paper-scissors-datomic "0.1.0-SNAPSHOT"
  :description "The game rock-paper-scissors implemented using Datomic"
  :url "https://github.com/jankronquist/rock-paper-scissors-with-datomic"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [com.datomic/datomic-free "0.8.4020.24"]
                 [rock-paper-scissors-core "1.0.0-SNAPSHOT"]]
  :main com.jayway.rps.datomic.main
  :plugins [[lein-ring "0.8.8"]]
  :ring {:handler com.jayway.rps.datomic.web/app}
  :profiles {:dev {:dependencies [[javax.servlet/servlet-api "2.5"]
                                  [ring-mock "0.1.5"]]}})

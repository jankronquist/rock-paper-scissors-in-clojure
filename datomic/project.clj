(defproject rock-paper-scissors-datomic "0.1.0-SNAPSHOT"
  :description "The game rock-paper-scissors implemented using Datomic"
  :url "https://github.com/jankronquist/rock-paper-scissors-with-datomic"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [com.datomic/datomic-free "0.8.4020.24"]]
  :main com.jayway.datomic.rps.main)

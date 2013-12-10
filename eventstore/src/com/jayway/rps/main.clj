(ns com.jayway.rps.main
  (:use [compojure.core] 
        [environ.core]
        [ring.middleware.session]
        [ring.middleware.keyword-params]
        [ring.middleware.params]
        [hiccup.core]
        [com.jayway.rps.facebook])
  (:require [compojure.route :as route]
            [com.jayway.rps.atom :as a]
            [com.jayway.rps.core :as c]
            [com.jayway.rps.framework :as f]
            [com.jayway.rps.domain :as d]
            [clj-http.client :as client]))

(defn -main [& args]
 (let [game-id (c/create-game com.jayway.rps.eventstore.web/rps)]
   (c/perform-command rps (c/->CreateGameCommand game-id "player-1" "rock"))
   (c/perform-command rps (c/->DecideMoveCommand game-id "player-2" "scissors"))
   (Thread/sleep 2000)
   (println (c/load-game rps game-id))))

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

(def rps
  (let [event-store (a/atom-event-store (env :event-store-uri))]
    (reify com.jayway.rps.core.RockPaperScissors
      (create-game [this] (str "game-" (.toString (java.util.UUID/randomUUID))))
      (perform-command [this command] (f/handle-command command event-store))
      (load-game [this game-id] 
        (let [uri (str (env :event-store-uri) "/projection/games/state?partition=" game-id)
              reply (client/get uri {:as :json})]
          (:body reply))))))

(defn -main [& args]
 (let [game-id (c/create-game rps)]
   (c/perform-command rps (c/->CreateGameCommand game-id "player-1" "rock"))
   (c/perform-command rps (c/->DecideMoveCommand game-id "player-2" "scissors"))
   (Thread/sleep 2000)
   (println (c/load-game rps game-id))))

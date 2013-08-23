(ns com.jayway.rps.main
  (:require [com.jayway.rps.core :as c]
            [com.jayway.rps.framework :as f] 
            [com.jayway.rps.domain :as d]
            [com.jayway.rps.atom :as a]))

(defn -main [uri aggregate-id & args]
  (let [store (a/atom-event-store uri)]
    (f/handle-command (d/->CreateGameCommand aggregate-id "player-1" "rock") store)
    (f/handle-command (d/->DecideMoveCommand aggregate-id "player-2" "scissors") store)
    (c/get-events (c/retrieve-event-stream store aggregate-id))))

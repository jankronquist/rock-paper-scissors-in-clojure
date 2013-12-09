(ns com.jayway.rps.eventstore.web
  (:use [environ.core])
  (:require [com.jayway.rps.atom :as a]
            [com.jayway.rps.framework :as f]
            [com.jayway.rps.domain :as d]
            [com.jayway.rps.web :as w]
            [clj-http.client :as client]))

(def app
  (let [event-store (a/atom-event-store (env :event-store-uri))]
    (w/create-app 
      (reify com.jayway.rps.core.RockPaperScissors
        (create-game [this] (str "game-" (.toString (java.util.UUID/randomUUID))))
        (perform-command [this command] (f/handle-command command event-store))
        (load-game [this game-id] 
          (let [uri (str (env :event-store-uri) "/projection/games/state?partition=" game-id)
                reply (client/get uri {:as :json})]
            (:body reply)))))))


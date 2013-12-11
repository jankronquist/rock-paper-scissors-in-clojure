(ns com.jayway.rps.datomic.web
  (:use [environ.core])
  (:require [datomic.api :as datomic]
            [com.jayway.rps.datomic.framework :as f]
            [com.jayway.rps.datomic.domain :as d]
            [com.jayway.rps.core :as c]
            [com.jayway.rps.web :as w]))

(datomic/create-database (env :datomic-url))
(def conn (datomic/connect (env :datomic-url)))
(f/initialize-schema conn)

(defn to-player-id [name]
  (let [result (datomic/q '[:find ?p :in $ ?name :where [?p :player/name ?name]] (datomic/db conn) name)]
    (if-let [existing (ffirst result)]
      existing
      (let [player-id (f/create-entity conn)]
        (println "creating player " player-id " with name " name)
        (f/handle-command (d/->SetPlayerNameCommand player-id name) conn)
        player-id))))

(def app
  (w/create-app 
    (reify com.jayway.rps.core.RockPaperScissors
      (create-game [this player-name] 
        (let [game-id (f/create-entity conn)
              command (c/->OnlyCreateGameCommand game-id (to-player-id player-name))]
          (f/handle-command command conn)
          game-id))
      (make-move [this game-id player-name move] 
          (f/handle-command (c/->DecideMoveCommand (Long/valueOf game-id) (to-player-id player-name) (keyword move)) conn))
      (load-game [this game-id] 
          (let [e (datomic/entity (datomic/db conn) (Long/valueOf game-id))
                game (datomic/touch e)
                result {:creator (:player/name (:game/created-by game))
                        :state (condp = (:game/state game)
                                 :game.state/started "open"
                                 :game.state/won "won"
                                 :game.state/tied "tied")
                        :moves (into {} (map 
                                          (fn [m] {(keyword (:player/name (:move/player m))) (:move/type m)}) 
                                          (:game/moves game)))}]
            (if-not (= "won" (:state result))
              result
              (assoc result
                     :winner (:player/name (:game/winner game))
                     :loser (:player/name (:game/loser game))))))
      (load-open-games 
        [this]
        (into {}
              (datomic/q '[:find ?game ?name 
                           :where [?game :game/state :game.state/started]
                                  [?game :game/created-by ?player]
                                  [?player :player/name ?name]] 
                         (datomic/db conn)))))))

(ns com.jayway.rps.core)

(defprotocol RockPaperScissors
  (create-game [this player-name])
  (make-move [this game-id player-name move])
  (load-game [this game-id])
  (to-player-id [this name] name))

(defrecord CreateGameCommand [aggregate-id player move])
(defrecord OnlyCreateGameCommand [aggregate-id player])
(defrecord DecideMoveCommand [aggregate-id player move])

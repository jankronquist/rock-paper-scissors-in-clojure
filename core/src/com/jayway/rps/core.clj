(ns com.jayway.rps.core)

(defprotocol RockPaperScissors
  (create-game [this])
  (perform-command [this command])
  (load-game [this game-id]))

(defrecord CreateGameCommand [aggregate-id player move])
(defrecord OnlyCreateGameCommand [aggregate-id player])
(defrecord DecideMoveCommand [aggregate-id player move])

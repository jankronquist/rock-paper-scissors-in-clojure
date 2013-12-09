(ns com.jayway.rps.datomic.domain
  (:require [datomic.api :as datomic]
            [com.jayway.rps.datomic.core :as c]))

(defrecord SetPlayerEmailCommand [aggregate-id email])

(defrecord CreateGameCommand [aggregate-id player move])
(defrecord OnlyCreateGameCommand [aggregate-id player])
(defrecord DecideMoveCommand [aggregate-id player move])

(defmulti compare-moves vector)
(defmethod compare-moves [:move.type/rock :move.type/rock] [x y] :tie)
(defmethod compare-moves [:move.type/rock :move.type/paper] [x y] :loss)
(defmethod compare-moves [:move.type/rock :move.type/scissors] [x y] :victory)
(defmethod compare-moves [:move.type/paper :move.type/rock] [x y] :victory)
(defmethod compare-moves [:move.type/paper :move.type/paper] [x y] :tie)
(defmethod compare-moves [:move.type/paper :move.type/scissors] [x y] :loss)
(defmethod compare-moves [:move.type/scissors :move.type/rock] [x y] :loss)
(defmethod compare-moves [:move.type/scissors :move.type/paper] [x y] :victory)
(defmethod compare-moves [:move.type/scissors :move.type/scissors] [x y] :tie)

(extend-protocol c/CommandHandler

  SetPlayerEmailCommand
  (c/perform [command state]
    [{:db/id (:aggregate-id command)
      :player/email (:email command)}])

  CreateGameCommand
  (c/perform [{:keys [player move aggregate-id]} state]
    (when (:game/state state)
      (throw (ex-info "Already in started" {:state state})))
    (let [move-id (datomic/tempid :db.part/user)]
      [{:db/id move-id
        :move/player player
        :move/type move}
       {:db/id aggregate-id
        :game/moves move-id
        :game/state :game.state/started
        :game/created-by player}]))

  OnlyCreateGameCommand
  (c/perform [{:keys [player aggregate-id]} state]
    (when (:game/state state)
      (throw (ex-info "Already in started" {:state state})))
    [{:db/id aggregate-id
      :game/state :game.state/started
      :game/created-by player}])

  DecideMoveCommand
  (c/perform [{:keys [player move aggregate-id]} state]
    (when-not (= (:game/state state) :game.state/started)
      (throw (ex-info "Incorrect state" {:state state})))
    (when (= (:db/id (:move/player (first (:game/moves state)))) player)
      (throw (ex-info "Cannot play against yourself" {:player player})))
    (let [other-move (:move/type (first (:game/moves state)))
          creator-id (:db/id (:game/created-by state))
          move-id (datomic/tempid :db.part/user)
          move-entity {:db/id move-id
                       :move/player player
                       :move/type move}
          aggregate-entity {:db/id aggregate-id
                            :game/moves move-id}]
      [move-entity
       (if-not other-move
         aggregate-entity
         (merge aggregate-entity
                (case (compare-moves move other-move)
                  :victory {:game/state :game.state/won
                            :game/winner player
                            :game/loser creator-id}
                  :loss {:game/state :game.state/won
                         :game/winner creator-id
                         :game/loser player}
                  :tie {:game/state :game.state/tied})))])))

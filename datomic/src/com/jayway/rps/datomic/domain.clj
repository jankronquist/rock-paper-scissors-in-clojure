(ns com.jayway.rps.datomic.domain
  (:require [datomic.api :as datomic]
            [com.jayway.rps.datomic.framework :as f]))

(defrecord SetPlayerNameCommand [aggregate-id name])

(defmulti compare-moves vector)
(defmethod compare-moves [:rock :rock] [x y] :tie)
(defmethod compare-moves [:rock :paper] [x y] :loss)
(defmethod compare-moves [:rock :scissors] [x y] :victory)
(defmethod compare-moves [:paper :rock] [x y] :victory)
(defmethod compare-moves [:paper :paper] [x y] :tie)
(defmethod compare-moves [:paper :scissors] [x y] :loss)
(defmethod compare-moves [:scissors :rock] [x y] :loss)
(defmethod compare-moves [:scissors :paper] [x y] :victory)
(defmethod compare-moves [:scissors :scissors] [x y] :tie)

(extend-protocol f/CommandHandler

  SetPlayerNameCommand
  (perform [command state]
    [{:db/id (:aggregate-id command)
      :player/name (:name command)}])

  com.jayway.rps.core.CreateGameCommand
  (perform [{:keys [player move aggregate-id]} state]
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

  com.jayway.rps.core.OnlyCreateGameCommand
  (perform [{:keys [player aggregate-id]} state]
    (when (:game/state state)
      (throw (ex-info "Already in started" {:state state})))
    [{:db/id aggregate-id
      :game/state :game.state/started
      :game/created-by player}])

  com.jayway.rps.core.DecideMoveCommand
  (perform [{:keys [player move aggregate-id]} state]
    (when-not (= (:game/state state) :game.state/started)
      (throw (ex-info "Incorrect state: " {:state (:game/state state)})))
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

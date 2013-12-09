(ns com.jayway.rps.datomic.test.functional
  (:require [clojure.test :refer :all]
            [datomic.api :as datomic]
            [com.jayway.rps.datomic.core :as c]
            [com.jayway.rps.datomic.framework :as f]
            [com.jayway.rps.datomic.domain :as domain]))

(def uri "datomic:mem://game")
(datomic/create-database uri)
(def conn (datomic/connect uri))

(f/initialize-schema conn)

(def ply1 (f/create-entity conn))
(def ply2 (f/create-entity conn))
(f/handle-command (domain/->SetPlayerEmailCommand ply1 "one@example.com") conn)
(f/handle-command (domain/->SetPlayerEmailCommand ply2 "two@example.com") conn)

(defn get-entity [entity-id]
  (datomic/touch (-> conn datomic/db (datomic/entity entity-id))))

(deftest functional-test
  (testing "rock beats scissors"
    (let [game-id (f/create-entity conn)]
      (f/handle-command (domain/->CreateGameCommand game-id ply1 :move.type/rock) conn)
      (f/handle-command (domain/->DecideMoveCommand game-id ply2 :move.type/scissors) conn)
      (is (= :game.state/won (:game/state (get-entity game-id))))
      (is (= (get-entity ply1) (:game/winner (get-entity game-id))))))
  (testing "separate create game"
    (let [game-id (f/create-entity conn)]
      (f/handle-command (domain/->OnlyCreateGameCommand game-id ply1) conn)
      (f/handle-command (domain/->DecideMoveCommand game-id ply1 :move.type/rock) conn)
      (f/handle-command (domain/->DecideMoveCommand game-id ply2 :move.type/scissors) conn)
      (is (= :game.state/won (:game/state (get-entity game-id))))
      (is (= (get-entity ply1) (:game/winner (get-entity game-id))))))
  (testing "tie"
    (let [game-id (f/create-entity conn)]
      (f/handle-command (domain/->CreateGameCommand game-id ply1 :move.type/paper) conn)
      (f/handle-command (domain/->DecideMoveCommand game-id ply2 :move.type/paper) conn)
      (is (= :game.state/tied (:game/state (get-entity game-id))))
      (is (= nil (:game/winner (get-entity game-id))))))
  (testing "should not play against self"
    (let [game-id (f/create-entity conn)]
      (f/handle-command (domain/->CreateGameCommand game-id ply1 :move.type/paper) conn)
      (is (thrown? Exception
                   (f/handle-command (domain/->DecideMoveCommand game-id ply1 :move.type/rock) conn)))))
  (testing "cannot start twice"
    (let [game-id (f/create-entity conn)]
      (f/handle-command (domain/->CreateGameCommand game-id ply1 :move.type/paper) conn)
      (is (thrown? Exception
                   (f/handle-command (domain/->CreateGameCommand game-id ply1 :move.type/paper) conn))))))

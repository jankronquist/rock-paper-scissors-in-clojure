(ns com.jayway.rps.datomic.main
  (:require [datomic.api :as datomic]
            [com.jayway.rps.datomic.framework :as f]
            [com.jayway.rps.core :as c]
            [com.jayway.rps.datomic.domain :as domain]))

(def uri "datomic:mem://game")
(datomic/create-database uri)
(def conn (datomic/connect uri))

(defn print-entity [entity-id]
  (let [e (datomic/entity (datomic/db conn) entity-id)]
    (println "entity: " (datomic/touch e))))

(f/initialize-schema conn)

(def ply1 (f/create-entity conn))
(def ply2 (f/create-entity conn))
(def game-id (f/create-entity conn))

(defn -main [& args]
  (f/handle-command (domain/->SetPlayerNameCommand ply1 "one") conn)
  (f/handle-command (domain/->SetPlayerNameCommand ply2 "two") conn)
  (f/handle-command (c/->OnlyCreateGameCommand game-id ply1) conn)
  (f/handle-command (c/->DecideMoveCommand game-id ply1 :move.type/rock) conn)
  (f/handle-command (c/->DecideMoveCommand game-id ply2 :move.type/scissors) conn)
  (print-entity game-id)
  (datomic/shutdown true))

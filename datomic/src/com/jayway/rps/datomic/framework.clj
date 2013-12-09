(ns com.jayway.rps.datomic.framework
  (:require [datomic.api :as datomic]
            [com.jayway.rps.datomic.core :as c]))

(defn initialize-schema [conn]
  (let [schema-tx (read-string (slurp "resources/schema.dtm"))]
    @(datomic/transact conn schema-tx)))

(defn create-entity [conn]
  "Returns the id of the new entity."
  (let [temp-id (datomic/tempid :db.part/user)
        optimistic-concurrency [:db.fn/cas temp-id :aggregate/version nil 0]
        tx @(datomic/transact conn [{:db/id temp-id} optimistic-concurrency])]
    (datomic/resolve-tempid (datomic/db conn) (:tempids tx) temp-id)))

(defn handle-command [{:keys [aggregate-id] :as command} conn]
  "Apply the command to its target aggregate using optimistic concurrency. Returns the datomic transaction."
  (let [state (datomic/entity (datomic/db conn) aggregate-id)
        modification (c/perform command state)
        old-version (:aggregate/version state)
        next-version ((fnil inc -1) old-version)
        optimistic-concurrency [:db.fn/cas aggregate-id :aggregate/version old-version next-version]
        tx @(datomic/transact conn (conj modification optimistic-concurrency))]
    tx))

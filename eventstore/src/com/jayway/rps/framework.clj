(ns com.jayway.rps.framework
  (:import java.util.ConcurrentModificationException
           java.util.concurrent.ConcurrentHashMap)
  (:require [com.jayway.rps.core :as c]))

(defmulti apply-event (fn [state event] (class event)))

(defprotocol EventStore
  (retrieve-event-stream [this aggregate-id])
  (append-events [this aggregate-id previous-event-stream events]))

(defn apply-events [state events]
  (reduce apply-event state events))

(defprotocol CommandHandler
  (perform [command state]))

(defn handle-command [command event-store]
  (let [event-stream (retrieve-event-stream event-store (:aggregate-id command))
        old-events (:events event-stream)
        current-state (apply-events {} old-events)
        new-events (perform command current-state)]
    (append-events event-store (:aggregate-id command) event-stream new-events)))

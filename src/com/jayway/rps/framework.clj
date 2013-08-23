(ns com.jayway.rps.framework
  (:import java.util.ConcurrentModificationException
           java.util.concurrent.ConcurrentHashMap)
  (:require [com.jayway.rps.core :as c]))

(defn apply-events [state events]
  (reduce c/apply-event state events))

(defn handle-command [command event-store]
  (let [event-stream (c/retrieve-event-stream event-store (:aggregate-id command))
        old-events (c/get-events event-stream)
        current-state (apply-events {} old-events)
        new-events (c/perform command current-state)]
    (c/append-events event-store (:aggregate-id command) event-stream new-events)))

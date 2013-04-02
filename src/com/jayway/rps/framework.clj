(ns com.jayway.rps.framework
  (:import java.util.ConcurrentModificationException
           java.util.concurrent.ConcurrentHashMap)
  (:require [com.jayway.rps.core :as c]))

(defprotocol EventStore
  (retrieve-event-stream [this aggregate-id])
  (append-events [this aggregate-id previous-event-stream events]))

(defrecord EventStream [version transactions])

(defn apply-events [state events]
  (reduce c/apply-event state events))

; no snapshots, simple version
(def in-memory-event-store
  (let [streams (ConcurrentHashMap.)
        empty-stream (->EventStream 0 [])]
    (reify EventStore
      (retrieve-event-stream [this aggregate-id]
        (if (.putIfAbsent streams aggregate-id empty-stream)
          (.get streams aggregate-id)
          empty-stream))

      (append-events [this aggregate-id previous-event-stream events]
        (let [next-event-stream (->EventStream (inc (:version previous-event-stream))
                                               (conj (:transactions previous-event-stream)
                                                     events))
              replaced (.replace streams aggregate-id previous-event-stream next-event-stream)]
          (when-not replaced (throw (ConcurrentModificationException.))))))))

(defn handle-command [command event-store]
  (let [event-stream (retrieve-event-stream event-store (:aggregate-id command))
        old-events (flatten (:transactions event-stream))
        current-state (apply-events {} old-events)
        new-events (c/perform command current-state)]
    (append-events event-store (:aggregate-id command) event-stream new-events)))

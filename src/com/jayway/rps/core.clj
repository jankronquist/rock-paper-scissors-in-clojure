(ns com.jayway.rps.core)

(defmulti apply-event (fn [state event] (class event)))

(defprotocol CommandHandler
  (perform [command state]))

(defprotocol EventStore
  (retrieve-event-stream [this aggregate-id])
  (append-events [this aggregate-id previous-event-stream events]))

(defprotocol EventStream
  (version [this])
  (get-events [this]))

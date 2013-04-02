(ns com.jayway.rps.core)

(defmulti apply-event (fn [state event] (class event)))

(defprotocol CommandHandler
  (perform [command state]))

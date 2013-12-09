(ns com.jayway.rps.datomic.core)

(defprotocol CommandHandler
  (perform [command state]))

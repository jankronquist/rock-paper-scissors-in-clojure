(ns com.jayway.rps.main
  (:require [com.jayway.rps.core :as c]
            [com.jayway.rps.framework :as f] 
            [com.jayway.rps.domain :as d]))

(defn -main [& args]
  (f/handle-command (d/->CreateGameCommand 1 :ply1 :rock) f/in-memory-event-store)
  (f/handle-command (d/->DecideMoveCommand 1 :ply2 :paper) f/in-memory-event-store)
  (f/retrieve-event-stream f/in-memory-event-store 1))

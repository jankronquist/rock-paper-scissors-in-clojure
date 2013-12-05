(ns com.jayway.rps.web
  (:use [compojure.core] 
        [environ.core]
        [ring.middleware.session]
        [ring.middleware.keyword-params]
        [ring.middleware.params]
        [hiccup.core]
        [com.jayway.rps.facebook])
  (:require [compojure.route :as route]
            [com.jayway.rps.atom :as a]
            [com.jayway.rps.framework :as f]
            [com.jayway.rps.domain :as d]
            [clj-http.client :as client]))

(def event-store (a/atom-event-store (env :event-store-uri)))

(defn new-aggregate-id [prefix]
  (str prefix "-" (.toString (java.util.UUID/randomUUID))))

(defn create-game [player-id move]
  (let [aggregate-id (new-aggregate-id "game")]
    (f/handle-command (d/->CreateGameCommand aggregate-id player-id move) event-store)
    aggregate-id))

(defn make-move [game-id player-id move]
  (f/handle-command (d/->DecideMoveCommand game-id player-id move) event-store))

(defn get-user [request]
  (get-in request [:session :me :name]))

(defn render-move-form 
  ([]
    (render-move-form "/games" "Create game"))
  ([game-id]
    (render-move-form (str "/games/" game-id) "Make move"))
  ([uri button-text]
    [:form {:action uri :method "post"} 
              [:select {:name "move"}
               [:option {:value "rock"} "Rock"]
               [:option {:value "paper"} "Paper"]
               [:option {:value "scissors"} "Scissors"]]
              [:input {:type "submit" :value button-text}]]))

(defn render-moves [moves]
  [:ul (map (fn [[player move]] [:li (str (name player) " moved " move)]) moves)])

(defn render-game [game-id player-id]
  (let [uri (str (env :event-store-uri) "/projection/games/state?partition=" game-id)
        reply (client/get uri {:as :json})
        game (:body reply)]
    (html [:body
           [:p (str "Created by " (:creator game))]
           (condp = (:state game)
             "open" (if-not (get-in game [:moves (keyword player-id)])
                      (render-move-form game-id)
                      [:p "Waiting..."])
             "won" [:div [:p (str "The winner is " (:winner game))] (render-moves (:moves game))]
             "tied" [:div [:p "Tie!"] (render-moves (:moves game))]
             "???")])))

(defroutes handler
  (GET "/server" [] (str "URI=" (env :event-store-uri)))
  (GET "/" [] (html [:body (render-move-form)]))
  (GET "/games" [] (html [:body (render-move-form)]))
  (POST "/games" [move :as r] 
        (let [game-id (create-game (get-user r) move)]
          (ring.util.response/redirect-after-post (str "/games/" game-id))))
  (POST "/games/:game-id" [game-id move :as r] 
        (make-move game-id (get-user r) move)
        (ring.util.response/redirect-after-post (str "/games/" game-id)))
  (GET "/games/:game-id" [game-id :as r] (render-game game-id (get-user r)))
  (route/not-found "<h1>Page not found</h1>"))

(defn wrap-log [handler level]
  (fn [request]
	  (println "REQUEST " level " : " request)
	  (let [response (handler request)]
      (println "RESPONSE " level " : " response)
      response)))

(def app 
    (-> handler 
        wrap-require-facebook-login
        wrap-session 
        wrap-keyword-params
        wrap-params))
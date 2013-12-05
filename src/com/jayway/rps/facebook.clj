(ns com.jayway.rps.facebook
  (:use [compojure.core] 
        [environ.core])
  (:require [compojure.route :as route]
            [clj-http.client :as client]))

(def facebook-oauth2
  {:authorization-uri "https://www.facebook.com/dialog/oauth"
   :access-token-uri "https://graph.facebook.com/oauth/access_token"
   :redirect-uri (env :facebook-redirect-uri)
   :client-id (env :facebook-client-id)
   :client-secret (env :facebook-client-secret)
   :access-query-param :access_token
   :scope ["email"]
   :grant-type "authorization_code"})

(defn facebook-redirect []
  (ring.util.response/redirect (str (:authorization-uri facebook-oauth2)
                                 "?client_id=" (:client-id facebook-oauth2) 
                                 "&redirect_uri=" (:redirect-uri facebook-oauth2))))

(defn parse-token-reply [reply]
  (reduce (fn [m arg] (let [spl (.split arg "=")] (assoc m (keyword (nth spl 0)) (nth spl 1)))) {} (.split reply "&")))

(defn facebook-get-token [code]
  (parse-token-reply (:body (client/get (:access-token-uri facebook-oauth2) 
                                         {:query-params {:client_id (:client-id facebook-oauth2)
                                                         :redirect_uri (:redirect-uri facebook-oauth2)
                                                         :client_secret (:client-secret facebook-oauth2)
                                                         :code code}}))))

(defn facebook-get-me [access_token] 
  (:body (client/get "https://graph.facebook.com/me" {:as :json
                                                      :query-params
                                                      {:format "json"
                                                       :access_token access_token}})))
(defn facebook-callback [code request]
  (let [access_token (facebook-get-token code)]
    (assoc (ring.util.response/redirect "/")
           :session {:me (facebook-get-me (:access_token access_token))
                     :access_token access_token})))

(defroutes callback-handler
  (GET "/oauth2-callback" [code :as r] (facebook-callback code r))
  (fn [request] (facebook-redirect)))

(defn wrap-require-facebook-login [handler]
  (fn [request]
    (if (get-in request [:session :me])
      (handler request)
      (callback-handler request))))

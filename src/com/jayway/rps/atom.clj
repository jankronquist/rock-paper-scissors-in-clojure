(ns com.jayway.rps.atom
  (:require [com.jayway.rps.core :as c]
            [clj-http.client :as client]
            [cheshire.core :as json]))

(defn new-uuid [] (.toString (java.util.UUID/randomUUID)))

(defn to-eventstore-format [event]
  {:eventId (new-uuid)
   :eventType (.replace (.getName (class event)) \. \_)
   :data event})

(defn uri-for-relation [relation links]
  (:uri (first (filter #(= relation (:relation %)) links))))

(defn construct-record [cs m]
  (when-let [f (resolve (symbol (clojure.string/replace cs #"\.(\w+)$" "/map->$1")))]
    (f m)))

(defn load-event [uri]
  (let [response (client/get uri {:as :json})
        event-data (get-in response [:body :content :data] {})
        event-type (.replace (get-in response [:body :content :eventType]) \_ \.)]
    (if event-type
      (construct-record event-type event-data)
      event-data)))

(declare load-events)

(defn load-events-from-list [response]
  (let [body (:body response)
        links (:links body)
        event-uris (reverse (map :id (:entries body)))
        previous-uri (uri-for-relation "previous" links)]
    (lazy-cat (map load-event event-uris) 
              (if previous-uri (lazy-seq (load-events previous-uri)))))) 

(defn load-events [uri]
  (load-events-from-list (client/get uri {:as :json})))

(def empty-stream (reify c/EventStream
                    (version [this] -1)
                    (get-events [this] [])))

(defn atom-event-store [uri]
  (letfn [(stream-uri [aggregate-id] (str uri "/streams/" aggregate-id))]
    (reify c/EventStore
      (retrieve-event-stream [this aggregate-id]
        ; three cases:
        ; 1) stream does not exist
        ; 2) stream exists, but has only a single page
        ; 3) stream exists and has multiple pages
        (let [root-uri (stream-uri aggregate-id)
              response (client/get root-uri {:as :json :throw-exceptions false})]
          (if-not (= 200 (:status response))
            empty-stream ; case 1
            (let [body (:body response)
                  links (:links body)
                  last-link (uri-for-relation "last" links)
                  events (if last-link
                               (load-events last-link) ; case 3
                               (load-events-from-list response))] ; case 2
              (reify c/EventStream
                (version [this] (dec (count events)))
                (get-events [this] events))))))
      
      (append-events 
        [this aggregate-id previous-event-stream events]
        (client/post (stream-uri aggregate-id)
                     {:body (json/generate-string (map to-eventstore-format events))
                      :content-type :json
                      :headers {"ES-ExpectedVersion" (str (c/version previous-event-stream))}})))))
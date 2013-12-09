(ns com.jayway.rps.atom
  (:require [com.jayway.rps.core :as c]
            [com.jayway.rps.framework :as f]
            [clj-http.client :as client]
            [cheshire.core :as json]))

(defn new-uuid [] (.toString (java.util.UUID/randomUUID)))

(defn to-eventstore-format [event]
  {:eventId (new-uuid)
   :eventType (.replace (.getName (class event)) \. \_)
   :data event})

(defn uri-for-relation [relation links]
  (:uri (first (filter #(= relation (:relation %)) links))))

(defn construct-record [type string]
  (when-let [f (resolve (symbol (clojure.string/replace type #"\.(\w+)$" "/map->$1")))]
    (f string)))

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

(def empty-stream {:version (fn [] -1) :events []})

; three cases:
; 1) stream does not exist
; 2) stream exists, but has only a single page
; 3) stream exists and has multiple pages
(defn load-events-from-feed [uri]
	(let [response (client/get uri {:as :json :throw-exceptions false})]
	  (if-not (= 200 (:status response))
	    empty-stream ; case 1
	    (let [body (:body response)
	          links (:links body)
	          last-link (uri-for-relation "last" links)
	          events (if last-link
	                       (load-events last-link) ; case 3
	                       (load-events-from-list response))] ; case 2
	      {:version (fn [] (dec (count events)))
	       :events events}))))

(defn atom-event-store [uri]
  (letfn [(stream-uri [aggregate-id] (str uri "/streams/" aggregate-id))]
    (reify f/EventStore
      (retrieve-event-stream [this aggregate-id]
        (load-events-from-feed (stream-uri aggregate-id)))
          
      (append-events 
        [this aggregate-id previous-event-stream events]
        (client/post (stream-uri aggregate-id)
                     {:body (json/generate-string (map to-eventstore-format events))
                      :content-type :json
                      :headers {"ES-ExpectedVersion" (str ((:version previous-event-stream)))}})))))
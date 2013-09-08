# rock-paper-scissors

The game rock-paper-scissors implemented using CQRS & Event Sourcing in Clojure with event storage in [EventStore](http://geteventstore.com).

Run using:

	lein run <eventStoreUri> <aggregate>
	
for example:

  lein run http://127.0.0.1:2113 game-1


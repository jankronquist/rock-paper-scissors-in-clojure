The game rock-paper-scissors implemented using CQRS & Event Sourcing in Clojure with event storage in [EventStore](http://geteventstore.com).

* Install EventStore. I have used Ubuntu 12.10 with Mono 3.2.3 using [this guide](http://forums.osgrid.org/viewtopic.php?f=14&t=4625).
* Create a file `.lein-env` based on provided template `.lein-env.template`. 
* Install [rock-paper-scissors-core](https://github.com/jankronquist/rock-paper-scissors-in-clojure/tree/master/core)

Run using:

	lein clean
	lein ring server

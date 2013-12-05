    fromAll()
    .foreachStream()

    .when({
        $init: function () {
            return { moves: {} }; // initial state
        },
      
        com_jayway_rps_domain_GameCreatedEvent: function(s, e) {
            s.creator = e.body.creator;
            s.state = "open";
            return s;
        },
                        
        com_jayway_rps_domain_MoveDecidedEvent: function(s, e) {
            s.moves[e.body.player] = e.body.move;
            return s;
        },
        
        com_jayway_rps_domain_GameTiedEvent: function(s, e) {
            s.state = "tied";
            return s;
        },
        
        com_jayway_rps_domain_GameWonEvent: function(s, e) {
            s.winner = e.body.winner;
            s.loser = e.body.loser;
            s.state = "won";
            return s;
        }
    });

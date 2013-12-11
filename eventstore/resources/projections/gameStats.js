// naive gameStats, does not really work, if there are "incorrect" events,
// for example duplicate GameCreatedEvents

fromAll()

.when({
    $init: function () {
        return { inProgress: 0, tied: 0, won:0 }; // initial state
    },
  
    com_jayway_rps_domain_GameCreatedEvent: function(s, e) {
        s.inProgress++;
        return s;
    },
                    
    com_jayway_rps_domain_GameTiedEvent: function(s, e) {
        s.tied++;
        s.inProgress--;
        return s;
    },
    
    com_jayway_rps_domain_GameWonEvent: function(s, e) {
        s.won++;
        s.inProgress--;
        return s;
    }
});
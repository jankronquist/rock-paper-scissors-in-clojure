fromAll()

.when({
    $init: function () {
        return {}; // initial state
    },
  
    com_jayway_rps_domain_GameCreatedEvent: function(s, e) {
        s[e.streamId] = e.body.creator;
        return s;
    },
                    
    com_jayway_rps_domain_GameTiedEvent: function(s, e) {
        delete s[e.streamId];
        return s;
    },
    
    com_jayway_rps_domain_GameWonEvent: function(s, e) {
        delete s[e.streamId];
        return s;
    }
});

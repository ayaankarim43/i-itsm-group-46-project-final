package com.example.demo;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public record IncidentEvent(Incident incident, EventType type, IncidentStatus previousState,
                            IncidentStatus newState, String actor, String description, LocalDateTime at) {

    public List<User> recipients() {
        List<User> r = new ArrayList<>();
        r.add(incident.getReporter());
        if (incident.getAssignedTo() != null) r.add(incident.getAssignedTo());
        return r;
    }
}



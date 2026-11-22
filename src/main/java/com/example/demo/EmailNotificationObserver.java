package com.example.demo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
class EmailNotificationObserver implements IncidentObserver {   // simulated email
    private static final Logger log = LoggerFactory.getLogger(EmailNotificationObserver.class);
    private static final Set<EventType> EMAILED =
            EnumSet.of(EventType.CREATED, EventType.ASSIGNED, EventType.RESOLVED, EventType.CLOSED,
                    EventType.SLA_WARNING, EventType.SLA_VIOLATED);

    @Override public void onEvent(IncidentEvent e) {
        if (!EMAILED.contains(e.type())) return;
        for (User u : e.recipients())
            log.info("[EMAIL] to {}: INC{} - {}", u.getEmail(), e.incident().getId(), e.description());
    }
}

package org.phuongnq.analyzer.service.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class IngestEvent extends ApplicationEvent {

    private final Long sid;

    public IngestEvent(Object source, Long sid) {
        super(source);
        this.sid = sid;
    }
}

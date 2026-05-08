package org.phuongnq.analyzer.utils;

import lombok.RequiredArgsConstructor;
import org.phuongnq.analyzer.service.event.IngestEvent;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class TestRunner implements ApplicationRunner {

    private final ApplicationEventPublisher eventPublisher;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        //eventPublisher.publishEvent(new IngestEvent(this, 2L));
    }
}

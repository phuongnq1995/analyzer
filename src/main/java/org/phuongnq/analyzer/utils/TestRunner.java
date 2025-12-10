package org.phuongnq.analyzer.utils;

import com.embabel.agent.api.common.autonomy.AgentInvocation;
import com.embabel.agent.core.AgentPlatform;
import com.embabel.agent.domain.io.UserInput;
import java.time.Duration;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.phuongnq.analyzer.agent.CampaignEfficiencyResult;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TestRunner implements ApplicationRunner {

    private final AgentPlatform agentPlatform;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        Instant start = Instant.now();

        var invocation = AgentInvocation.create(agentPlatform, CampaignEfficiencyResult.class);

        String userPrompt = "Analyze the marketing efficiency for campaign 'post3' from '2025-12-02' to '2025-12-08' and provide insights campaign performance.";

        UserInput userInput = new UserInput(userPrompt, Instant.now());

        log.info("Starting Agent Invocation for Marketing Efficiency Analysis...");

        CampaignEfficiencyResult result = invocation.invoke(userInput);

        log.info("Agent Invocation Completed. Result: {}", result);

        log.info("Completed invocate Agent in {} s", Duration.between(start, Instant.now()).getSeconds());
    }
}

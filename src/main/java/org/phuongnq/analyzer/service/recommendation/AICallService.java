package org.phuongnq.analyzer.service.recommendation;

import com.google.genai.errors.ClientException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.phuongnq.analyzer.query.model.evaluate.EfficiencyResults;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AICallService {

    private final AIRateLimitService aiRateLimitService;
    private final ChatClient chatClient;

    @Transactional(readOnly = true)
    @Retryable(retryFor = ClientException.class, maxAttempts = 3, backoff = @Backoff(delay = 5000, multiplier = 2))
    public EfficiencyResults callAI(String userPrompt, String systemPrompt) {

        String model = aiRateLimitService.getAvailableModel();

        try {
            ChatOptions options = ChatOptions.builder()
                .model(model)
                .build();

            log.info("Calling AI model: {}", model);

            return chatClient.prompt()
                .user(userPrompt)
                .system(systemPrompt)
                .options(options) // Apply the specific model options
                .advisors(new SimpleLoggerAdvisor())
                .call()
                .entity(EfficiencyResults.class);

        } catch (Exception e) {
            log.error("Error calling AI model {}: ", model, e);
            aiRateLimitService.lockModel(model);
            throw new RuntimeException(e);
        }
    }
}

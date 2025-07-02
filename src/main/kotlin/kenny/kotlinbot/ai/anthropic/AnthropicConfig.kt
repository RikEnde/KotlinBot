package kenny.kotlinbot.ai.anthropic

import org.springframework.ai.anthropic.AnthropicChatOptions
import org.springframework.ai.model.anthropic.autoconfigure.AnthropicChatProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class AnthropicConfig {

    @Bean
    fun anthropicChatOptions(chatProperties: AnthropicChatProperties): AnthropicChatOptions {
        val defaultOptions = chatProperties.options
        return AnthropicChatOptions.builder()
            .model(defaultOptions.model)
            .temperature(defaultOptions.temperature)
            .maxTokens(defaultOptions.maxTokens)
            .build()
    }
}
package kenny.kotlinbot.ai.anthropic

import org.springframework.ai.anthropic.AnthropicChatOptions
import org.springframework.ai.autoconfigure.anthropic.AnthropicChatProperties
import org.springframework.ai.autoconfigure.openai.OpenAiChatProperties
import org.springframework.ai.openai.OpenAiChatOptions
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class AnthropicConfig {

    @Bean
    fun anthropicChatOptions(chatProperties: AnthropicChatProperties): AnthropicChatOptions {
        val defaultOptions = chatProperties.options
        return AnthropicChatOptions.builder()
            .withModel(defaultOptions.model)
            .withTemperature(defaultOptions.temperature)
            .withMaxTokens(defaultOptions.maxTokens)
            .build()
    }
}
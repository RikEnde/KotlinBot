package kenny.kotlinbot.ai.anthropic

import kenny.kotlinbot.ai.ModelsClient
import org.springframework.ai.anthropic.AnthropicChatOptions
import org.springframework.ai.anthropic.api.AnthropicApi
import org.springframework.ai.model.anthropic.autoconfigure.AnthropicChatProperties
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.web.client.RestClient

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

    @Bean
    fun anthropicModelsClient(
        @Value("\${spring.ai.anthropic.api-key}") apiKey: String,
    ): ModelsClient =
        RestClient.builder()
            .baseUrl("${AnthropicApi.DEFAULT_BASE_URL}/v1")
            .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer $apiKey")
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .build()
            .let(::ModelsClient)
}
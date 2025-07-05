package kenny.kotlinbot.ai.openai

import kenny.kotlinbot.ai.ModelsClient
import org.springframework.ai.image.ImageOptions
import org.springframework.ai.model.openai.autoconfigure.OpenAiChatProperties
import org.springframework.ai.model.openai.autoconfigure.OpenAiImageProperties
import org.springframework.ai.openai.OpenAiChatOptions
import org.springframework.ai.openai.OpenAiImageOptions
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.web.client.ClientHttpRequestFactories
import org.springframework.boot.web.client.ClientHttpRequestFactorySettings
import org.springframework.boot.web.client.RestClientCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.web.client.RestClient
import java.time.Duration

@Configuration
class OpenAIConfig {

    @Bean
    fun openAiChatOptions(chatProperties: OpenAiChatProperties): OpenAiChatOptions {
        val defaultOptions = chatProperties.options
        return OpenAiChatOptions.builder()
            .model(defaultOptions.model)
            .temperature(defaultOptions.temperature)
            .maxTokens(defaultOptions.maxTokens)
            .topP(defaultOptions.topP)
            .frequencyPenalty(defaultOptions.frequencyPenalty)
            .presencePenalty(defaultOptions.presencePenalty)
            .stop(defaultOptions.stop)
            .build()
    }

    @Bean
    fun dalleImageOptions(imageProperties: OpenAiImageProperties): ImageOptions {
        return OpenAiImageOptions.builder()
            .height(imageProperties.options.height)
            .width(imageProperties.options.width)
            .N(imageProperties.options.n)
            .quality(imageProperties.options.quality)
            .model(imageProperties.options.model)
            .build()
    }

    @Bean
    fun restClientCustomizer(): RestClientCustomizer {
        return RestClientCustomizer { restClientBuilder: RestClient.Builder ->
            restClientBuilder
                .requestFactory(
                    ClientHttpRequestFactories.get(
                        ClientHttpRequestFactorySettings.DEFAULTS
                            .withConnectTimeout(Duration.ofSeconds(30))
                            .withReadTimeout(Duration.ofSeconds(30))
                    )
                )
        }
    }

    @Bean
    fun openAIModelsClient(
        @Value("\${spring.ai.openai.base-url:https://api.openai.com}") baseUrl: String,
        @Value("\${spring.ai.openai.api-key}") apiKey: String
    ):
            ModelsClient =
        RestClient.builder()
            .baseUrl("${baseUrl}/v1")
            .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer $apiKey")
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .build()
            .let(::ModelsClient)
}
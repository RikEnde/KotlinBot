package kenny.kotlinbot.ai.openai

import org.springframework.ai.autoconfigure.openai.OpenAiChatProperties
import org.springframework.ai.autoconfigure.openai.OpenAiImageProperties
import org.springframework.ai.image.ImageOptions
import org.springframework.ai.openai.OpenAiChatOptions
import org.springframework.ai.openai.OpenAiImageOptions
import org.springframework.boot.web.client.ClientHttpRequestFactories
import org.springframework.boot.web.client.ClientHttpRequestFactorySettings
import org.springframework.boot.web.client.RestClientCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestClient
import java.time.Duration

@Configuration
class OpenAIConfig {

    @Bean
    fun chatOptions(chatProperties: OpenAiChatProperties): OpenAiChatOptions {
        val defaultOptions = chatProperties.options
        return OpenAiChatOptions.builder()
            .withModel(defaultOptions.model)
            .withTemperature(defaultOptions.temperature)
            .withMaxTokens(defaultOptions.maxTokens)
            .withTopP(defaultOptions.topP)
            .withFrequencyPenalty(defaultOptions.frequencyPenalty)
            .withPresencePenalty(defaultOptions.presencePenalty)
            .withStop(defaultOptions.stop)
            .build()
    }

    @Bean
    fun dalleImageOptions(imageProperties: OpenAiImageProperties): ImageOptions {
        return OpenAiImageOptions.builder()
            .withHeight(imageProperties.options.height)
            .withWidth(imageProperties.options.width)
            .withN(imageProperties.options.n)
            .withQuality(imageProperties.options.quality)
            .withModel(imageProperties.options.model)
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
}
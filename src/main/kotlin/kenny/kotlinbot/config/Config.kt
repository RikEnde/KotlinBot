package kenny.kotlinbot.config

import kenny.kotlinbot.discord.DiscordEventListener
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.requests.GatewayIntent
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction
import org.springframework.ai.autoconfigure.openai.OpenAiChatProperties
import org.springframework.ai.autoconfigure.openai.OpenAiImageProperties
import org.springframework.ai.image.ImageOptions
import org.springframework.ai.openai.OpenAiChatOptions
import org.springframework.ai.openai.OpenAiImageOptions
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.web.client.ClientHttpRequestFactories
import org.springframework.boot.web.client.ClientHttpRequestFactorySettings
import org.springframework.boot.web.client.RestClientCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestClient
import java.time.Duration

@Configuration
class Config {
    @Value("\${application.discord.token}")
    var discordToken: String? = null

    @Bean
    fun commands(jda: JDA): CommandListUpdateAction {
        val commands = jda.updateCommands()
            .addCommands(
                Commands.slash("temp", "Set the AI  temperature")
                    .addOption(OptionType.NUMBER, "temp", "Temperature for the AI", true),

                Commands.slash("tokens", "Set the AI max tokens")
                    .addOption(OptionType.INTEGER, "tokens", "Temperature for the AI", true),

                Commands.slash("image", "Ask dall-e for an image")
                    .addOption(OptionType.STRING, "prompt", "Prompt for the image", false),

                Commands.slash("prompt", "Ask AI for a response")
                    .addOption(OptionType.STRING, "prompt", "Prompt to respond to", true),

                Commands.slash("role", "Ask AI to assume this role")
                    .addOption(OptionType.STRING, "role", "Role for the AI to assume", false),

                Commands.slash("summary", "Summarize the user's conversation so far"),

                Commands.slash("forget", "Forget the user's conversation so far"),
                Commands.slash("history", "List the user's stored images")
                    .addOption(OptionType.INTEGER, "n", "History line number", false)
            )
        commands.queue()
        return commands
    }

    @Bean
    fun jda(discordEventListener: DiscordEventListener): JDA {
        return JDABuilder.createDefault(discordToken)
            .addEventListeners(discordEventListener)
            .enableIntents(GatewayIntent.MESSAGE_CONTENT)
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
    fun dalleImageOptions(imageProperties: OpenAiImageProperties) : ImageOptions {
        return OpenAiImageOptions.builder()
            .withHeight(imageProperties.options.height)
            .withWidth(imageProperties.options.width)
            .withN(imageProperties.options.n)
            .withQuality(imageProperties.options.quality)
            .withModel(imageProperties.options.model)
            .build()
    }
}

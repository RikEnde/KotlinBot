package kenny.kotlinbot.discord

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.requests.GatewayIntent
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class DiscordConfig {
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
                    .addOption(OptionType.INTEGER, "n", "History line number", false),

                Commands.slash("models", "List the models available in the active API")
                    .addOption(OptionType.STRING, "model", "Model name", false)
            )
        commands.queue()
        return commands
    }

    @Bean
    fun jda(discordEventListener: DiscordEventListener, properties: DiscordProperties): JDA {
        return JDABuilder.createDefault(properties.token)
            .addEventListeners(discordEventListener)
            .enableIntents(GatewayIntent.MESSAGE_CONTENT)
            .build()
    }
}
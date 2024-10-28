package kenny.kotlinbot.discord

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "discord")
class DiscordProperties {
    lateinit var token: String
    var messageSize: Int = 2000
}
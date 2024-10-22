package kenny.kotlinbot.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "application")
class ApplicationProperties {
    lateinit var discord: DiscordProperties
    lateinit var defaultRole: String

    class DiscordProperties {
        lateinit var token: String
        var messageSize: Int = 2000
    }
}
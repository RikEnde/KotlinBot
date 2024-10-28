package kenny.kotlinbot.ai

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "application")
class ChatProperties {
    lateinit var defaultRole: String
}
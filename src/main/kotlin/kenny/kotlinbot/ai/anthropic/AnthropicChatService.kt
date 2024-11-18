package kenny.kotlinbot.ai.anthropic

import kenny.kotlinbot.ai.BaseChatService
import kenny.kotlinbot.ai.ChatProperties
import kenny.kotlinbot.ai.ChatService
import org.springframework.ai.anthropic.AnthropicChatModel
import org.springframework.ai.anthropic.AnthropicChatOptions
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service

@Profile("anthropic")
@Service
class AnthropicChatService(val anthropicChatOptions: AnthropicChatOptions,
                           override val chatModel: AnthropicChatModel,
                           override val properties: ChatProperties
): BaseChatService(anthropicChatOptions, chatModel, properties), ChatService {

    override fun temperature(temp: Double): String {
        if (temp > 0 && temp <= 2.0) {
            anthropicChatOptions.temperature = temp
        }
        return "Temperature set to ${anthropicChatOptions.temperature}"
    }

    override fun maxTokens(tokens: Int): String {
        if (tokens in 1..4096) {
            anthropicChatOptions.maxTokens = tokens
        }
        return "MaxTokens set to ${anthropicChatOptions.maxTokens}"
    }
}
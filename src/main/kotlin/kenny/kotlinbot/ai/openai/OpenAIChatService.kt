package kenny.kotlinbot.ai.openai

import kenny.kotlinbot.ai.BaseChatService
import kenny.kotlinbot.ai.ChatProperties
import kenny.kotlinbot.ai.ChatService
import kenny.kotlinbot.storage.ChatStorageService
import org.springframework.ai.openai.OpenAiChatModel
import org.springframework.ai.openai.OpenAiChatOptions
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service

@Profile("openai")
@Service
class OpenAIChatService(
    val openAiChatOptions: OpenAiChatOptions,
    override val chatModel: OpenAiChatModel,
    override val properties: ChatProperties,
    override val chatStorage: ChatStorageService
) : BaseChatService(openAiChatOptions, chatModel, properties, chatStorage), ChatService {

    override fun temperature(temp: Double): String {
        if (temp > 0 && temp <= 2.0) {
            openAiChatOptions.temperature = temp
        }
        return "Temperature set to ${openAiChatOptions.temperature}"
    }

    override fun maxTokens(tokens: Int): String {
        if (tokens in 1..4096) {
            openAiChatOptions.maxTokens = tokens
        }
        return "MaxTokens set to ${openAiChatOptions.maxTokens}"
    }
}
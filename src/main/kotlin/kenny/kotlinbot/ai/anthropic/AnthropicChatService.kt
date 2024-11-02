package kenny.kotlinbot.ai.anthropic

import kenny.kotlinbot.ai.ChatProperties
import kenny.kotlinbot.ai.ChatService
import org.springframework.ai.anthropic.AnthropicChatModel
import org.springframework.ai.anthropic.AnthropicChatOptions
import org.springframework.ai.chat.messages.AssistantMessage
import org.springframework.ai.chat.messages.Message
import org.springframework.ai.chat.messages.UserMessage
import org.springframework.ai.chat.model.ChatResponse
import org.springframework.ai.chat.prompt.Prompt
import org.springframework.ai.chat.prompt.SystemPromptTemplate
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import java.util.concurrent.ConcurrentHashMap

@Profile("anthropic")
@Service
class AnthropicChatService(val anthropicChatOptions: AnthropicChatOptions,
                           val chatModel: AnthropicChatModel,
                           val properties: ChatProperties
): ChatService {

    private var systemMessage: Message? = null
    private val chats = ConcurrentHashMap<String, List<Message>>()

    fun userChats(userName: String) : List<Message> = chats.getOrPut(userName) { emptyList() }
    fun systemMessage() : Message {
        if (this.systemMessage == null) {
            return systemMessage("ChatBot")
        }
        return this.systemMessage!!
    }

    fun systemMessage(role: String): Message {
        val message = SystemPromptTemplate(properties.defaultRole).createMessage(mapOf("role" to role))
        this.systemMessage = message
        return message
    }

    override fun chat(prompt: String, userName: String): String {
        val messages = userChats(userName) + UserMessage(prompt)

        val response: ChatResponse? = chatModel.call(Prompt(messages.plus(systemMessage()), anthropicChatOptions))

        response?.let {
            chats[userName] = messages + AssistantMessage(it.result.output.content)
        }

        return response?.result?.output?.content ?: "No response from OpenAI API."
    }

    override fun randomRole(): String =
        chatModel.call("Choose a random role for an AI chatbot in one paragraph")

    override fun role(role: String?): String {
        var prompt: String? = role
        if (prompt.isNullOrBlank()) {
            prompt = randomRole()
        }
        if (prompt.isNotBlank()) {
            systemMessage(prompt)
        }

        return "Role is now $prompt"
    }

    override fun forget(userName: String): String {
        if (chats.containsKey(userName)) {
            chats.remove(userName)
            return "Forget all chats for $userName"
        } else {
            return "I have no memory of $userName"
        }
    }

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
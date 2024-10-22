package kenny.kotlinbot.ai.openai

import kenny.kotlinbot.ai.ChatService
import org.springframework.ai.chat.messages.AssistantMessage
import org.springframework.ai.chat.messages.Message
import org.springframework.ai.chat.messages.UserMessage
import org.springframework.ai.chat.model.ChatResponse
import org.springframework.ai.chat.prompt.Prompt
import org.springframework.ai.chat.prompt.SystemPromptTemplate
import org.springframework.ai.openai.OpenAiChatModel
import org.springframework.ai.openai.OpenAiChatOptions
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import java.util.concurrent.ConcurrentHashMap

@Profile("openai")
@Service
class OpenAIChatService(val chatOptions: OpenAiChatOptions,
                        val chatModel: OpenAiChatModel,
                        @Value("\${application.default-role}") val defaultRole: String) : ChatService {
    private var systemMessage: Message? = null
    private val chats = ConcurrentHashMap<String, List<Message>>()

    init {
        if (this.systemMessage == null) {
            systemMessage("ChatBot")
        }
    }

    private final fun systemMessage(role: String) {
        this.systemMessage = SystemPromptTemplate(defaultRole).createMessage(mapOf("role" to role))
    }

    override fun chat(prompt: String, userName: String): String {
        val messages = chats.getOrPut(userName) { emptyList() } + UserMessage(prompt)

        val response : ChatResponse? = chatModel.call(Prompt(messages.plus(systemMessage), chatOptions))

        response?.let {
            chats[userName] = messages + AssistantMessage(it.result.output.content)
        }

        return response?.result?.output?.content ?: "No response from OpenAI API."
    }

    override fun randomRole(): String =
        chatModel.call("Choose a random role for an AI chatbot in one paragraph")

    override fun role(role: String?): String {
        var prompt : String? = role
        if (prompt.isNullOrBlank()) {
            prompt =  randomRole()
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
            chatOptions.temperature = temp
        }
        return "Temperature set to ${chatOptions.temperature}"
    }

    override fun maxTokens(tokens: Int): String {
        if (tokens in 1..4096) {
            chatOptions.maxTokens = tokens
        }
        return "MaxTokens set to ${chatOptions.maxTokens}"
    }
}
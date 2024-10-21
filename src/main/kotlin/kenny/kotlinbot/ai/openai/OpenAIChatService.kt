package kenny.kotlinbot.ai.openai

import kenny.kotlinbot.ai.ChatService
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

@Profile("openai")
@Service
class OpenAIChatService(val chatOptions: OpenAiChatOptions,
                        val chatModel: OpenAiChatModel,
                        @Value("\${application.default-role}") val defaultRole: String) : ChatService {
    private var systemMessage: Message? = null
    init {
        // Add your custom initialization statements here
        if (this.systemMessage == null) {
            systemMessage("ChatBot")
        }
    }

    private final fun systemMessage(role: String) {
        this.systemMessage = SystemPromptTemplate(defaultRole)
            .createMessage(mapOf("role" to role))
    }

    override fun chat(prompt: String, userName: String): String {
        val messages = listOf(
            UserMessage(prompt)
        )

        val response : ChatResponse? = chatModel.call(Prompt(messages, chatOptions))

        return response?.result?.output?.content ?: "No response from OpenAI API."
    }

    override fun randomRole(): String {
        return chatModel.call("Choose a random role for an AI chatbot in one paragraph")
    }

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
//        if (chats.containsKey(userName)) {
//            chats.remove(userName)
//            return "Forget all chats for $userName"
//        } else {
//            return "I have no memory of $userName"
//        }
        TODO("Not yet implemented")
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
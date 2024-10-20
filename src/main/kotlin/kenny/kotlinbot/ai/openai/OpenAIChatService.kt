package kenny.kotlinbot.ai.openai

import kenny.kotlinbot.ai.ChatService
import org.springframework.ai.chat.messages.Message
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

    override fun chat(messages: List<Message>): String {

        val prompt = Prompt(messages, chatOptions)

        val response : ChatResponse? = chatModel.call(prompt)

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

        return "Role is now '%s'".format(prompt)
    }

    override fun temperature(temp: Double): String {
        chatOptions.temperature = temp
        return "Temperature set to $temp"
    }

    override fun maxTokens(tokens: Int): String {
        chatOptions.maxTokens = tokens
        return "MaxTokens set to $tokens"
    }
}
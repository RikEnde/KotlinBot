package kenny.kotlinbot.ai

import org.springframework.ai.chat.messages.AssistantMessage
import org.springframework.ai.chat.messages.Message
import org.springframework.ai.chat.messages.UserMessage
import org.springframework.ai.chat.model.ChatModel
import org.springframework.ai.chat.model.ChatResponse
import org.springframework.ai.chat.prompt.ChatOptions
import org.springframework.ai.chat.prompt.Prompt
import org.springframework.ai.chat.prompt.SystemPromptTemplate
import java.util.concurrent.ConcurrentHashMap

abstract class BaseChatService(val chatOptions: ChatOptions,
                               open val chatModel: ChatModel,
                               open val properties: ChatProperties): ChatService {
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

    override fun imageChat(userName: String, prompt: String, revisedPrompt: String) {
        chats[userName] = userChats(userName) +
                UserMessage("generate the following image: $prompt") +
                AssistantMessage("generated the following image: $revisedPrompt")
    }

    override fun chat(prompt: String, userName: String): String {
        val messages = userChats(userName) + UserMessage(prompt)

        val response: ChatResponse? = chatModel.call(Prompt(messages.plus(systemMessage()), chatOptions))

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
}
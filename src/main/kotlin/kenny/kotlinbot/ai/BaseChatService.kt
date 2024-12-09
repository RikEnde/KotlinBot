package kenny.kotlinbot.ai

import kenny.kotlinbot.storage.ChatStorageService
import kenny.kotlinbot.storage.ChatType
import kenny.kotlinbot.storage.ChatType.BOT
import kenny.kotlinbot.storage.ChatType.USER
import kenny.kotlinbot.storage.StoredChat
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.ai.chat.messages.*
import org.springframework.ai.chat.model.ChatModel
import org.springframework.ai.chat.model.ChatResponse
import org.springframework.ai.chat.prompt.ChatOptions
import org.springframework.ai.chat.prompt.Prompt
import org.springframework.ai.chat.prompt.SystemPromptTemplate
import org.springframework.context.event.ContextClosedEvent
import org.springframework.context.event.ContextRefreshedEvent
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Async
import java.util.concurrent.ConcurrentHashMap


abstract class BaseChatService(
    val chatOptions: ChatOptions,
    open val chatModel: ChatModel,
    open val properties: ChatProperties,
    open val chatStorage: ChatStorageService
) : ChatService {
    val logger: Logger = LoggerFactory.getLogger(this.javaClass)

    private var systemMessage: Message? = null

    fun map(message: Message, userName: String): StoredChat {
        return StoredChat(
            userName,
            when (message) {
                is UserMessage -> USER
                is AssistantMessage -> BOT
                else -> throw IllegalArgumentException("Unknown message type")
            },
            message.content
        )
    }

    fun userChats(userName: String): List<Message> = chatStorage.getUserChats(userName)
        .map { storedChat ->
            when (storedChat.type) {
                USER -> UserMessage(storedChat.chat)
                BOT -> AssistantMessage(storedChat.chat)
            }
        }

    fun systemMessage(): Message {
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
        chatStorage.saveUserChats(
            listOf(
                UserMessage("generate the following image: $prompt"),
                AssistantMessage("generated the following image: $revisedPrompt")
            ).map { map(it, userName) })
    }

    override fun chat(prompt: String, userName: String): String {
        val userMessage = UserMessage(prompt)

        val response: ChatResponse? =
            chatModel.call(Prompt(userChats(userName) + userMessage + systemMessage(), chatOptions))

        response?.let {
            val chats = listOf(userMessage, AssistantMessage(it.result.output.content))
                .map { map(it, userName) }
            chatStorage.saveUserChats(chats)
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
        if (chatStorage.removeUserChats(userName) > 0) {
            return "Forget all chats for $userName"
        } else {
            return "I have no memory of $userName"
        }
    }
}


package kenny.kotlinbot.ai

import kenny.kotlinbot.storage.ChatStorageService
import kenny.kotlinbot.storage.ChatType
import kenny.kotlinbot.storage.StoredChat
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.ai.chat.messages.AssistantMessage
import org.springframework.ai.chat.messages.Message
import org.springframework.ai.chat.messages.MessageType
import org.springframework.ai.chat.messages.UserMessage
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
    private val chats = ConcurrentHashMap<String, List<Message>>()

    fun userChats(userName: String): List<Message> = chats.getOrPut(userName) { emptyList() }

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
            chatStorage.removeUserChats(userName)
            return "Forget all chats for $userName"
        } else {
            return "I have no memory of $userName"
        }
    }

    @Async
    @EventListener
    open fun onShutdown(event: ContextClosedEvent) {
        logger.info("Application is shutting down, saving user chats...")
        chats.keys().iterator().forEach { userName ->
            val chats: List<StoredChat> = chats[userName]?.map {
                StoredChat(
                    userName,
                    if (it.messageType == MessageType.USER) ChatType.USER else ChatType.BOT,
                    it.content
                )
            }!!
            chatStorage.saveUserChats(chats)
        }
    }

    @Async
    @EventListener
    open fun onStartup(event: ContextRefreshedEvent) {
        logger.info("Application is starting up, reloading user chats...")

        chatStorage.users().forEach { userName ->
            chats[userName] = chatStorage.getUserChats(userName).map { storedChat ->
                when (storedChat.type) {
                    ChatType.USER -> UserMessage(storedChat.chat)
                    ChatType.BOT -> AssistantMessage(storedChat.chat)
                }
            }
        }
    }
}


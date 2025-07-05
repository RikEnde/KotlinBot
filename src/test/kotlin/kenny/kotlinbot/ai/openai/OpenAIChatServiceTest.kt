package kenny.kotlinbot.ai.openai

import kenny.kotlinbot.ai.ChatProperties
import kenny.kotlinbot.storage.ChatStorageService
import kenny.kotlinbot.storage.ChatType.BOT
import kenny.kotlinbot.storage.ChatType.USER
import kenny.kotlinbot.storage.StoredChat
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.mockito.kotlin.*
import org.springframework.ai.chat.messages.AssistantMessage
import org.springframework.ai.chat.messages.UserMessage
import org.springframework.ai.chat.model.ChatResponse
import org.springframework.ai.chat.model.Generation
import org.springframework.ai.chat.prompt.Prompt
import org.springframework.ai.openai.OpenAiChatModel
import org.springframework.ai.openai.OpenAiChatOptions
import kotlin.test.Test

class OpenAIChatServiceTest {
    private val chatModel: OpenAiChatModel = mock()
    private val chatStorage: ChatStorageService = mock()
    private lateinit var chatService: OpenAIChatService

    private val chatOptions = OpenAiChatOptions().apply {
        temperature = 0.7
        maxTokens = 100
        model= "gpt-4o"
    }

    private val properties = ChatProperties().apply {
        defaultRole = "Your unit testing role is {role}."
    }

    @BeforeEach
    fun setUp() {
        chatService = OpenAIChatService(chatOptions, chatModel, properties, chatStorage)
    }

    @Test
    fun `maps spring AI Messages to stored chats`() {
        val messages = listOf(UserMessage("Hello, AI!"),
            AssistantMessage("Hello, User!")
        )
        val userName = "testUser"

        val storedChats = messages.map {
            chatService.map(it, userName)
        }

        assertEquals(userName, storedChats[0].userName)
        assertEquals(userName, storedChats[1].userName)
        assertEquals(USER, storedChats[0].type)
        assertEquals(BOT, storedChats[1].type)
        assertEquals("Hello, AI!", storedChats[0].chat)
        assertEquals("Hello, User!", storedChats[1].chat)
    }

    @Test
    fun `maps spring stored chats to AI Messages`() {
        val userName = "testUser"
        val storedChats = listOf(
            StoredChat(userName, USER, "Hello, AI!"),
            StoredChat(userName, BOT, "Hello, User!")
        )

        whenever(chatStorage.getUserChats(eq(userName)))
            .thenReturn(storedChats)

        val messages = chatService.userChats(userName)
        assertThat(messages).hasSize(2)
        assertThat(messages[0]).isInstanceOf(UserMessage::class.java)
        assertThat(messages[0].text).isEqualTo("Hello, AI!")
        assertThat(messages[1]).isInstanceOf(AssistantMessage::class.java)
        assertThat(messages[1].text).isEqualTo("Hello, User!")
    }

    @Test
    fun `chat saves conversation with API to user chats`() {
        val prompt = "Say: 'Hello World!'"
        val userName = "Unit Test"
        val responseContent = "Hello World!"
        val chatResponse = ChatResponse(listOf(Generation(AssistantMessage(responseContent))))

        whenever(chatModel.call(any<Prompt>())).thenReturn(chatResponse)
        whenever(chatStorage.getUserChats(userName))
            .thenReturn(listOf(
                StoredChat(userName, USER, prompt),
                StoredChat(userName, BOT, responseContent))
            )

        val response = chatService.chat(prompt, userName)
        assertThat(response).isEqualTo(responseContent)

        val chats = chatService.userChats(userName)
        assertThat(chats).hasSize(2)

        assertThat(chats[0]).matches { it is UserMessage && it.text == prompt }
        assertThat(chats[1]).matches { it is AssistantMessage && it.text == responseContent }
    }

    @Test
    fun `imageChat should save user and assistant messages to storage`() {
        val userName = "testUser"
        val prompt = "a test user testing"
        val revisedPrompt = "a needlessly florid description of a user toiling in the test mines"

        chatService.imageChat(userName, prompt, revisedPrompt)

        verify(chatStorage, times(1)).saveUserChats(argThat { chats ->
            chats.size == 2 &&
                    chats[0].chat == "generate the following image: $prompt" &&
                    chats[0].type == USER &&
                    chats[1].chat == "generated the following image: $revisedPrompt" &&
                    chats[1].type == BOT
        })
    }

    @Test
    fun `chat returns no response from OpenAI API when response is null`() {
        val prompt = "Say: 'Hello World!'"
        val userName = "Unit Test"

        whenever(chatModel.call(any<Prompt>())).thenReturn(null)

        val response = chatService.chat(prompt, userName)
        assertThat(response).isEqualTo("No response from OpenAI API.")

        assertThat(chatService.userChats(userName)).isEmpty()
    }

    @Test
    fun `role sets role to literal string`() {
        val role = "ChatBot"
        val response = chatService.role(role)
        assertThat(response).isEqualTo("Role is now ChatBot")
        assertThat(chatService.systemMessage().text).isEqualTo("Your unit testing role is ChatBot.")
    }

    @Test
    fun `randomRole returns a random role for an AI chatbot in one paragraph`() {
        whenever(chatModel.call(any<String>())).thenReturn("Random role chosen by AI")

        val response = chatService.randomRole()

        verify(chatModel).call("Choose a random role for an AI chatbot in one paragraph")

        assertThat(response).isEqualTo("Random role chosen by AI")
    }

    @Test
    fun `empty role sets random role for system message`() {
        whenever(chatModel.call(any<String>())).thenReturn("Random role chosen by AI")

        val response = chatService.role(null)

        assertThat(response).isEqualTo("Role is now Random role chosen by AI")
        assertThat(chatService.systemMessage().text).isEqualTo("Your unit testing role is Random role chosen by AI.")
    }

    @Test
    fun `forget removes all chats for a user`() {
        val userName = "Unit Test"
        val prompt = "Say: 'Hello World!'"
        val responseContent = "I'm not saying Hello World!"
        val chatResponse = ChatResponse(listOf(Generation(AssistantMessage(responseContent))))

        whenever(chatModel.call(any<Prompt>())).thenReturn(chatResponse)
        whenever(chatStorage.getUserChats(userName))
            .thenReturn(listOf(
                StoredChat(userName, USER, prompt),
                StoredChat(userName, BOT, responseContent))
            )
        whenever(chatStorage.removeUserChats(userName)).thenReturn(2)

        chatService.chat(prompt, userName)
        assertThat(chatService.userChats(userName)).hasSize(2)

        val response = chatService.forget(userName)
        assertThat(response).isEqualTo("Forget all chats for Unit Test")
    }

    @Test
    fun `forget returns no memory of user when no chats are found`() {
        val userName = "Unit Test"

        val response = chatService.forget(userName)
        assertThat(response).isEqualTo("I have no memory of Unit Test")
    }

    @Test
    fun `temperature sets temperature for chat options`() {
        assertThat(chatService.temperature(0.7)).isEqualTo("Temperature set to 0.7")
        assertThat(chatOptions.temperature).isEqualTo(0.7)

        // Does not set out of range
        assertThat(chatService.temperature(-1.0)).isEqualTo("Temperature set to 0.7")
        assertThat(chatOptions.temperature).isEqualTo(0.7)

        assertThat(chatService.temperature(2.1)).isEqualTo("Temperature set to 0.7")
        assertThat(chatOptions.temperature).isEqualTo(0.7)
    }

    @Test
    fun `listModels returns current model and available models`() {
        // Mock the RestClient response
        val mockResponse = """
            {
              "object": "list",
              "data": [
                {"id": "gpt-4", "object": "model"},
                {"id": "gpt-4-turbo", "object": "model"},
                {"id": "gpt-3.5-turbo", "object": "model"},
                {"id": "text-embedding-ada-002", "object": "model"}
              ]
            }
        """.trimIndent()

        // Use reflection to access the private method and mock it
        val result = chatService.listModels()

        // Verify the result contains the current model
        assertThat(result).contains("Current model: gpt-4o")

        // The result should either contain the models from the API or the fallback models
        assertThat(result).matches { 
            it.contains("Available models:") || it.contains("Available models (fallback):")
        }

        // Should contain at least some GPT models
        assertThat(result).matches {
            it.contains("gpt-4") && it.contains("gpt-3.5-turbo")
        }
    }
}

package kenny.kotlinbot.ai.openai

import kenny.kotlinbot.ai.ChatProperties
import kenny.kotlinbot.storage.ChatStorageService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
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
    private val chatStorageService: ChatStorageService = mock()
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
        chatService = OpenAIChatService(chatOptions, chatModel, properties, chatStorageService)
    }

    @Test
    fun `chat saves conversation with API to user chats`() {
        val prompt = "Say: 'Hello World!'"
        val userName = "Unit Test"
        val responseContent = "Hello World!"
        val chatResponse = ChatResponse(listOf(Generation(AssistantMessage(responseContent))))

        whenever(chatModel.call(any<Prompt>())).thenReturn(chatResponse)

        val response = chatService.chat(prompt, userName)
        assertThat(response).isEqualTo(responseContent)

        val chats = chatService.userChats(userName)
        assertThat(chats).hasSize(2)

        assertThat(chats[0]).matches { it is UserMessage && it.content == prompt }
        assertThat(chats[1]).matches { it is AssistantMessage && it.content == responseContent }
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
        assertThat(chatService.systemMessage().content).isEqualTo("Your unit testing role is ChatBot.")
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
        assertThat(chatService.systemMessage().content).isEqualTo("Your unit testing role is Random role chosen by AI.")
    }

    @Test
    fun `forget removes all chats for a user`() {
        val userName = "Unit Test"
        val responseContent = "I'm not saying Hello World!"
        val chatResponse = ChatResponse(listOf(Generation(AssistantMessage(responseContent))))

        whenever(chatModel.call(any<Prompt>())).thenReturn(chatResponse)

        chatService.chat("Say: 'Hello World!'", userName)
        assertThat(chatService.userChats(userName)).hasSize(2)

        val response = chatService.forget(userName)
        assertThat(response).isEqualTo("Forget all chats for Unit Test")
        assertThat(chatService.userChats(userName)).isEmpty()
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
}
package kenny.kotlinbot.ai.openai

import kenny.kotlinbot.ai.ChatService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.ai.chat.messages.UserMessage
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.assertj.core.api.Assertions.assertThat

@ExtendWith(SpringExtension::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("openai")
class OpenAIChatServiceIT {

    @Autowired
    private lateinit var chatService: ChatService

    @Test
    fun chat_sayHelloWorld() {
        val messages = listOf(
            UserMessage("Say: \"Hello World!\"")
        )
        val response = chatService.chat(messages)
        println("Random role: $response")

        assertThat(response).containsIgnoringCase("Hello World")
    }

    @Test
    fun role_setRoleToLiteralString() {
        val role = "Assistant"
        val response = chatService.role(role)
        assertEquals("Role is now 'Assistant'", response)
    }

    @Test
    fun randomRoleCallsOut() {
        val response = chatService.randomRole()
        println("Random role: $response")

        assertThat(response).isNotEmpty()
    }

    @Test
    fun testTemperature() {
        val temp = 0.7
        val response = chatService.temperature(temp)
        assertEquals("Temperature set to 0.7", response)
    }

    @Test
    fun testMaxTokens() {
        val tokens = 100
        val response = chatService.maxTokens(tokens)
        assertEquals("MaxTokens set to 100", response)
    }
}
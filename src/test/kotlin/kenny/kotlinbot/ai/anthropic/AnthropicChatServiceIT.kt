package kenny.kotlinbot.ai.anthropic

import kenny.kotlinbot.ai.ChatService
import kenny.kotlinbot.storage.jpa.ChatStorageServicePostgres
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertEquals
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.ActiveProfiles
import kotlin.test.Test

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE, classes = [AnthropicTestConfig::class])
@ActiveProfiles("anthropic")
class AnthropicChatServiceIT {
    @Autowired
    private lateinit var chatService: ChatService

    @MockBean
    lateinit var chatStorageService: ChatStorageServicePostgres

    @Test
    fun chat_sayHelloWorld() {
        val response = chatService.chat("Say: 'Hello World!'", "Unit Test")
        println("Response: $response")

        assertThat(response).containsIgnoringCase("Hello World")
    }

    @Test
    fun role_setRoleToLiteralString() {
        val role = "Assistant"
        val response = chatService.role(role)
        assertEquals("Role is now Assistant", response)
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
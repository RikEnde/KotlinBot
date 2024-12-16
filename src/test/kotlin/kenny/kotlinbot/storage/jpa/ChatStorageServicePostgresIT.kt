package kenny.kotlinbot.storage.jpa

import kenny.kotlinbot.storage.ChatStorageService
import kenny.kotlinbot.storage.ChatType
import kenny.kotlinbot.storage.StoredChat
import org.assertj.core.api.Assertions.assertThat
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.transaction.annotation.Transactional
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import kotlin.test.Test

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE, classes = [TestConfig::class])
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Transactional
@ActiveProfiles("postgres")
@Testcontainers
class ChatStorageServicePostgresIT {
    @Autowired
    private lateinit var chatStorageService: ChatStorageService

    companion object {
        // Define the PostgreSQLContainer
        @Container
        val postgresContainer: PostgreSQLContainer<*> = PostgreSQLContainer("postgres:15.2")
            .apply {
                withDatabaseName("images")
                withUsername("postgres")
                withPassword("postgres")
            }

        // Set dynamic properties for the Spring context to use Testcontainers PostgreSQL instance
        @JvmStatic
        @DynamicPropertySource
        fun postgresProperties(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url", postgresContainer::getJdbcUrl)
            registry.add("spring.datasource.username", postgresContainer::getUsername)
            registry.add("spring.datasource.password", postgresContainer::getPassword)
            registry.add("spring.datasource.password", postgresContainer::getPassword)
            registry.add("spring.jpa.hibernate.ddl-auto") { "create" }
        }
    }

    val chats = listOf(
        StoredChat("UnitTest", ChatType.USER, "prompt 1"),
        StoredChat("UnitTest", ChatType.BOT, "reply 1"),
        StoredChat("UnitTest", ChatType.USER, "prompt 2"),
        StoredChat("UnitTest", ChatType.BOT, "reply 2"),
        StoredChat("UnitTest2", ChatType.USER, "prompt 3")
    )

    @Test
    fun storeLoadUserChats() {
        assertThat(chatStorageService.getUserChats("UnitTest")).isEmpty()

        chatStorageService.saveUserChats(chats)

        val userChats = chatStorageService.getUserChats("UnitTest")
        assertThat(userChats).containsExactlyInAnyOrderElementsOf(chats.filter { it.userName == "UnitTest" })
    }

    @Test
    fun users() {
        assertThat(chatStorageService.users()).isEmpty()

        chatStorageService.saveUserChats(chats)

        assertThat(chatStorageService.users()).containsExactlyInAnyOrder("UnitTest", "UnitTest2")
    }

    @Test
    fun forgetUser() {
        chatStorageService.saveUserChats(chats)

        assertThat(chatStorageService.users()).containsExactlyInAnyOrder("UnitTest", "UnitTest2")

        chatStorageService.removeUserChats("UnitTest2")

        assertThat(chatStorageService.users()).containsExactlyInAnyOrder("UnitTest")

        chatStorageService.removeUserChats("UnitTest")

        assertThat(chatStorageService.users()).isEmpty()
    }
}
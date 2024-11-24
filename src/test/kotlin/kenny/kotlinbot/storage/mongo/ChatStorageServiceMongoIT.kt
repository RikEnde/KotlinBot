package kenny.kotlinbot.storage.mongo

import kenny.kotlinbot.storage.ChatType
import kenny.kotlinbot.storage.StoredChat
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.MongoDBContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

@DataMongoTest
@Import(ChatStorageServiceMongo::class)
@ActiveProfiles("mongo")
@Testcontainers
class ChatStorageServiceMongoIT {
    companion object {
        // Define and start MongoDBContainer
        @Container
        val mongoContainer: MongoDBContainer = MongoDBContainer("mongo:6.0.9")

        // Dynamically set MongoDB URI for the Spring context
        @JvmStatic
        @DynamicPropertySource
        fun mongoProperties(registry: DynamicPropertyRegistry) {
            registry.add("spring.data.mongodb.uri", mongoContainer::getReplicaSetUrl)
        }
    }

    @Autowired
    private lateinit var chatStorageService: ChatStorageServiceMongo

    @BeforeEach
    @AfterEach
    fun clearTestData() {
        chatStorageService.removeUserChats("UnitTest")
        chatStorageService.removeUserChats("UnitTest2")
    }

    val chats = listOf(
        StoredChat("UnitTest", ChatType.USER, "prompt 1"),
        StoredChat("UnitTest", ChatType.BOT, "reply 1"),
        StoredChat("UnitTest", ChatType.USER, "prompt 2"),
        StoredChat("UnitTest", ChatType.BOT, "reply 2"),
        StoredChat("UnitTest2", ChatType.USER, "prompt 3")
    )

    @Test
    fun testStoreAndLoad() {
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
package kenny.kotlinbot.storage.mongo

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.MongoDBContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.util.*
import kotlin.test.Test

@DataMongoTest
@Import(StorageServiceMongo::class)
@ActiveProfiles("mongo")
@Testcontainers
class MongoStorageServiceIT {
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
    private lateinit var storageService: StorageServiceMongo

    val IMAGE_URL = "file:src/test/resources/image.jpg"
    val testUser1 = "UnitTest"
    val testUser2 = "UnitTest2"
    
    @BeforeEach
    @AfterEach
    fun clearTestData() {
        storageService.deleteUserData(testUser1)
        storageService.deleteUserData(testUser2)
    }

    @Test
    fun testStoreAndLoad() {
        val prompt = "testPrompt"
        val revisedPrompt = "revisedTestPrompt"

        val storedImage = storageService.store(IMAGE_URL, testUser1, prompt, revisedPrompt)
        assertThat(storedImage.metaData.userName).isEqualTo(testUser1)
        assertThat(storedImage.metaData.prompt).isEqualTo(prompt)
        assertThat(storedImage.metaData.revisedPrompt).isEqualTo(revisedPrompt)
        assertThat(storedImage.fileName).isEqualTo("image.jpg")
        assertThat(storedImage.id).isNotNull()

        storageService.load(storedImage.id).use { inputStream ->
            val loadedImageData = inputStream.readAllBytes()
            assertThat(loadedImageData).isNotEmpty
            assertThat(loadedImageData.contentHashCode()).isEqualTo(302502126)
        }
    }

    @Test
    fun testDeleteUserData() {
        val userName = testUser1
        storageService.store(IMAGE_URL, userName, "prompt1", "revisedPrompt1")
        storageService.store(IMAGE_URL, userName, "prompt2", "revisedPrompt2")

        assertThat(storageService.list(userName)).hasSize(2)

        storageService.deleteUserData(userName)
        assertThat(storageService.list(userName)).isEmpty()
    }

    @Test
    fun testFindByPrompt() {
        val userName = testUser1
        val prompt = "UnitTestPrompt:${UUID.randomUUID()}"
        storageService.store(IMAGE_URL, userName, prompt, "revisedPrompt")

        val foundImages = storageService.findByPrompt(userName, prompt)
        assertThat(foundImages).hasSize(1)
        assertThat(foundImages.first().metaData.prompt).isEqualTo(prompt)
    }

    @Test
    fun testUpdateDiscordUrl() {
        val prompt = "prompt"
        val revisedPrompt = "revisedTestPrompt"

        val storedImage = storageService.store(IMAGE_URL, testUser1, prompt, revisedPrompt)

        val discordUrl = "http://discord.com/image.jpg"
        storageService.update(storedImage.id, discordUrl)

        val updatedImage = storageService.findById(storedImage.id)
        assertThat(updatedImage?.metaData?.discordUrl).isEqualTo(discordUrl)
    }

    @Test
    fun list() {
        assertThat(storageService.list(testUser1)).isEmpty()

        storageService.store(IMAGE_URL, testUser1, "Prompt", "Revised Prompt")
        storageService.store(IMAGE_URL, testUser1, "Prompt2", "Revised Prompt2")

        val result = storageService.list(testUser1)
        result.forEach { println(it) }

        assertThat(result).hasSize(2)
    }

    @Test
    fun store() {
        assertThat(storageService.list(testUser1)).isEmpty()

        storageService.store(IMAGE_URL, testUser1, "Prompt", "Revised Prompt")

        assertThat(storageService.list(testUser1)).hasSize(1)
    }

    @Test
    fun findByPrompt() {
        assertThat(storageService.list(testUser1)).isEmpty()

        storageService.store(IMAGE_URL, testUser1, "Prompt", "Revised Prompt")

        val result = storageService.findByPrompt(testUser1, "Prompt")
        result.forEach { println(it) }

        assertThat(result).hasSize(1)
        assertThat(result.first().metaData.prompt).isEqualTo("Prompt")
    }

    @Test
    fun load() {
        assertThat(storageService.list(testUser1)).isEmpty()

        storageService.store(IMAGE_URL, testUser1, "Prompt", "Revised Prompt")

        val result = storageService.list(testUser1)
        assertThat(result).hasSize(1)

        storageService.load(result.first().id).use { inputStream ->
            val loadedImageData = inputStream.readAllBytes()
            assertThat(loadedImageData).isNotEmpty
            assertThat(loadedImageData.contentHashCode()).isEqualTo(302502126)
        }
    }

    @Test
    fun update() {
        assertThat(storageService.list(testUser1)).isEmpty()
        storageService.store(IMAGE_URL, testUser1, "Prompt", "Revised Prompt")

        val result = storageService.list(testUser1)

        storageService.update(result.first().id, "https://example.com")
        val updatedResult = storageService.list(testUser1)
        assertThat(updatedResult).hasSize(1)
        assertThat(updatedResult.first().metaData.discordUrl).isEqualTo("https://example.com")
    }

    @Test
    fun deleteUserData() {
        assertThat(storageService.list(testUser1)).isEmpty()

        storageService.store(IMAGE_URL, testUser1, "Prompt", "Revised Prompt")
        storageService.store(IMAGE_URL, testUser1, "Prompt2", "Revised Prompt2")
        storageService.store(IMAGE_URL, testUser2, "Prompt", "Revised Prompt")
        storageService.store(IMAGE_URL, testUser2, "Prompt2", "Revised Prompt2")

        assertThat(storageService.list(testUser1)).hasSize(2)
        assertThat(storageService.list(testUser2)).hasSize(2)

        storageService.deleteUserData(testUser1)
        assertThat(storageService.list(testUser1)).isEmpty()
        assertThat(storageService.list(testUser2)).hasSize(2)

        storageService.deleteUserData(testUser2)
        assertThat(storageService.list(testUser2)).isEmpty()
    }
}
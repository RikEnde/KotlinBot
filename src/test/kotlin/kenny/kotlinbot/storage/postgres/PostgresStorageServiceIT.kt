package kenny.kotlinbot.storage.postgres

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.transaction.annotation.Transactional
import java.util.*
import kotlin.test.Test

@ExtendWith(SpringExtension::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Transactional
@ActiveProfiles("openai", "postgres")
class PostgresStorageServiceIT {

    @Autowired
    private lateinit var storageService: PostgresStorageService

    private val IMAGE_URL = "file:src/test/resources/image.jpg"

    @Test
    fun testStoreAndLoad() {
        val userName = "UnitTest"
        val prompt = "testPrompt"
        val revisedPrompt = "revisedTestPrompt"

        val storedImage = storageService.store(IMAGE_URL, userName, prompt, revisedPrompt)
        assertThat(storedImage.metaData.userName).isEqualTo(userName)
        assertThat(storedImage.metaData.prompt).isEqualTo(prompt)
        assertThat(storedImage.metaData.revisedPrompt).isEqualTo(revisedPrompt)
        assertThat(storedImage.fileName).isEqualTo("image.jpg")
        assertThat(storedImage.id).isNotNull()

        val loadedImageStream = storageService.load(storedImage.id)
        val loadedImageData = loadedImageStream.readAllBytes()

        assertThat(loadedImageData.contentHashCode()).isEqualTo(302502126)
    }

    @Test
    fun testDeleteUserData() {
        val userName = "UnitTest"
        storageService.store(IMAGE_URL, userName, "prompt1", "revisedPrompt1")
        storageService.store(IMAGE_URL, userName, "prompt2", "revisedPrompt2")

        assertThat(storageService.list(userName)).hasSize(2)

        storageService.deleteUserData(userName)
        val storedImages = storageService.list(userName)
        assertThat(storedImages).isEmpty()
    }

    @Test
    fun testFindByPrompt() {
        val userName = "UnitTest"
        val prompt = "UnitTestPrompt:${UUID.randomUUID()}"
        storageService.store(IMAGE_URL, userName, prompt, "revisedPrompt")

        val foundImages = storageService.findByPrompt(userName, prompt)
        assertThat(foundImages).hasSize(1)
        assertThat(foundImages.first().metaData.prompt).isEqualTo(prompt)
    }

    @Test
    fun testUpdateDiscordUrl() {
        val userName = "UnitTest"
        val prompt = "prompt"
        val revisedPrompt = "revisedTestPrompt"

        val storedImage = storageService.store(IMAGE_URL, userName, prompt, revisedPrompt)

        val discordUrl = "http://discord.com/image.jpg"
        storageService.update(storedImage.id, discordUrl)

        val updatedImage = storageService.findById(storedImage.id)
        assertThat(updatedImage?.metaData?.discordUrl).isEqualTo(discordUrl)
    }
}

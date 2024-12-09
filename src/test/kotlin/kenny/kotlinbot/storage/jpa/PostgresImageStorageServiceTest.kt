package kenny.kotlinbot.storage.jpa

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.argThat
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import kotlin.test.Test

class PostgresImageStorageServiceTest {
    val imageRepository: ImageRepositoryPostgres = mock()
    lateinit var storageService: ImageStorageServicePostgres

    val IMAGE_URL = "file:src/test/resources/image.jpg"

    @BeforeEach
    fun setUp() {
        storageService = ImageStorageServicePostgres(imageRepository)
    }

    @Test
    fun `store image`() {
        val userName = "Unit Test"
        val prompt = "A tiny banana"
        val revisedPrompt = "A tiny banana that is microscopic and bla bla"

        whenever(imageRepository.save(any<Image>())).thenAnswer { it.arguments[0] }
        val storedImageResult = storageService.store(IMAGE_URL, userName, prompt, revisedPrompt)

        verify(imageRepository).save(argThat { image ->
            image.imageData.contentHashCode() == 302502126 &&
                    image.fileName == "image.jpg" &&
                    image.userName == userName &&
                    image.prompt == prompt &&
                    image.revisedPrompt == revisedPrompt
        })

        assertThat(storedImageResult.metaData.userName).isEqualTo(userName)
        assertThat(storedImageResult.metaData.prompt).isEqualTo(prompt)
        assertThat(storedImageResult.metaData.revisedPrompt).isEqualTo(revisedPrompt)
        assertThat(storedImageResult.fileName).isEqualTo("image.jpg")
        assertThat(storedImageResult.id).isNotNull()
    }
}
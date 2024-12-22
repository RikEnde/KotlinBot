package kenny.kotlinbot.ai.openai

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.mockito.kotlin.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.ai.image.Image
import org.springframework.ai.image.ImageGeneration
import org.springframework.ai.image.ImagePrompt
import org.springframework.ai.image.ImageResponse
import org.springframework.ai.openai.OpenAiImageModel
import org.springframework.ai.openai.OpenAiImageOptions
import org.springframework.ai.openai.metadata.OpenAiImageGenerationMetadata
import kotlin.test.Test

class DallEImageServiceTest {
    private val imageModel: OpenAiImageModel = mock()
    private lateinit var imageService: DallEImageService

    val dalleImageOptions = OpenAiImageOptions().apply {
        n = 1
        model = "dall-e-3"
        quality = "hd"
        height = 1024
        width = 1024
    }

    @BeforeEach
    fun setUp() {
        imageService = DallEImageService(dalleImageOptions, imageModel)
    }

    @Test
    fun `image returns image result`() {
        val prompt = "A unit tester sitting at a computer"
        val revisedPrompt = "A unit tester sitting at a computer in a modern, well-lit office environment bla bla"
        val userName = "Unit Test"
        val url = "https://example.com/image.jpg"
        val imageResponse = ImageResponse(listOf(ImageGeneration(Image(url, "123"), OpenAiImageGenerationMetadata(revisedPrompt))))

        whenever(imageModel.call(any<ImagePrompt>())).thenReturn(imageResponse)

        val imageResult = imageService.image(prompt, userName)

        assertThat(imageResult.url).isEqualTo(url)
        assertThat(imageResult.prompt).isEqualTo(prompt)
        assertThat(imageResult.revisedPrompt).isEqualTo(revisedPrompt)
    }

}
package kenny.kotlinbot.ai.openai

import kenny.kotlinbot.ai.ImageService
import org.assertj.core.api.Assertions.assertThat
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import kotlin.test.Test

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE, classes = [OpenAITestConfig::class])
@ActiveProfiles("dalle")
class DallEImageServiceIT {

    @Autowired
    private lateinit var imageService: ImageService

    @Test
    fun image_generateImage() {
        val prompt = "flower in a unit test"
        val userName = "Unit Test"
        val response = imageService.image(prompt, userName)
        println("Image response: ${response.revisedPrompt}")
        println("Image response: ${response.url}")

        assertThat(response.url).startsWith("https://")
    }
}
package kenny.kotlinbot.ai.openai

import kenny.kotlinbot.ai.ImageResult
import kenny.kotlinbot.ai.ImageService
import org.springframework.ai.image.*
import org.springframework.ai.openai.metadata.OpenAiImageGenerationMetadata
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service

@Profile("openai")
@Service
class DallEImageService(
    private val dalleImageOptions: ImageOptions,
    private val imageModel: ImageModel
) : ImageService {
    override fun image(prompt: String, userName: String): ImageResult {
        val imagePrompt = ImagePrompt(prompt, dalleImageOptions)

        val response = imageModel.call(imagePrompt)

        val image: Image = response.getResult().output
        val metaData: ImageGenerationMetadata = response.getResult().metadata
        val revisedPrompt = (metaData as OpenAiImageGenerationMetadata).revisedPrompt

        return ImageResult(image.url, prompt, revisedPrompt)
    }
}
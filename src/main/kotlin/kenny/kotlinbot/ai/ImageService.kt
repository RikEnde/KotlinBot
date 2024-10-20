package kenny.kotlinbot.ai

interface ImageService {
    fun image(prompt: String, userName: String) : ImageResult
}

data class ImageResult (val url: String, val prompt: String, val revisedPrompt: String)
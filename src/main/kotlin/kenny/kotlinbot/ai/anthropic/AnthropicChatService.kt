package kenny.kotlinbot.ai.anthropic

import kenny.kotlinbot.ai.BaseChatService
import kenny.kotlinbot.ai.ChatProperties
import kenny.kotlinbot.ai.ChatService
import kenny.kotlinbot.ai.ModelsClient
import kenny.kotlinbot.ai.ModelsClient.Model
import kenny.kotlinbot.storage.ChatStorageService
import org.springframework.ai.anthropic.AnthropicChatModel
import org.springframework.ai.anthropic.AnthropicChatOptions
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import java.time.Instant

@Profile("anthropic")
@Service
class AnthropicChatService(
    val anthropicChatOptions: AnthropicChatOptions,
    override val chatModel: AnthropicChatModel,
    override val properties: ChatProperties,
    override val chatStorage: ChatStorageService,
    val anthropicModelsClient: ModelsClient
) : BaseChatService(anthropicChatOptions, chatModel, properties, chatStorage), ChatService {

    override fun temperature(temp: Double): String {
        if (temp > 0 && temp <= 2.0) {
            anthropicChatOptions.temperature = temp
        }
        return "Temperature set to ${anthropicChatOptions.temperature}"
    }

    override fun maxTokens(tokens: Int): String {
        if (tokens in 1..4096) {
            anthropicChatOptions.maxTokens = tokens
        }
        return "MaxTokens set to ${anthropicChatOptions.maxTokens}"
    }

    override fun listModels(): List<Model> {
        val currentModel = anthropicChatOptions.model

        try {
            val modelsList = anthropicModelsClient.listModelIds()

            return modelsList
        } catch (e: Exception) {
            return listOf(Model(currentModel!!, "bot", Instant.now(), "anthropic"))
        }
    }

    override fun selectModel(model: String, userName: String): String {
        val models = listModels()
        val selectedModel = models.find { it.id == model }

        if (selectedModel != null) {
            anthropicChatOptions.model = selectedModel.id
            return "Model set to ${selectedModel.id}"
        } else {
            return "Model $model not found"
        }
    }
}

package kenny.kotlinbot.ai.openai

import kenny.kotlinbot.ai.BaseChatService
import kenny.kotlinbot.ai.ChatProperties
import kenny.kotlinbot.ai.ChatService
import kenny.kotlinbot.ai.ModelsClient
import kenny.kotlinbot.ai.ModelsClient.Model
import kenny.kotlinbot.storage.ChatStorageService
import org.springframework.ai.openai.OpenAiChatModel
import org.springframework.ai.openai.OpenAiChatOptions
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.temporal.ChronoUnit

@Profile("openai")
@Service
class OpenAIChatService(
    val openAiChatOptions: OpenAiChatOptions,
    override val chatModel: OpenAiChatModel,
    override val properties: ChatProperties,
    override val chatStorage: ChatStorageService,
    val openAIModelsClient: ModelsClient
) : BaseChatService(openAiChatOptions, chatModel, properties, chatStorage), ChatService {

    override fun temperature(temp: Double): String {
        if (temp > 0 && temp <= 2.0) {
            openAiChatOptions.temperature = temp
        }
        return "Temperature set to ${openAiChatOptions.temperature}"
    }

    override fun maxTokens(tokens: Int): String {
        if (tokens in 1..4096) {
            openAiChatOptions.maxTokens = tokens
        }
        return "MaxTokens set to ${openAiChatOptions.maxTokens}"
    }

    override fun listModels(): List<Model> {
        val currentModel = openAiChatOptions.model

        try {
            val models = openAIModelsClient.listModelIds()

            // We don't want to see all of their model names,
            // remove the date-tagged ones and the > 1yr old ones
            val filtered = models
                .filter { it.created.isAfter(Instant.now().minus(365, ChronoUnit.DAYS)) }
//                .filter { !it.id.contains("preview") }
                .filter { !Regex("^.*\\d+-\\d+-\\d+$").matches(it.id) }
                .sortedByDescending{ it.created }

//            println(filtered.joinToString("\n") { "${it.id}\t\t${it.created}" })

            return filtered
        } catch (e: Exception) {
            e.printStackTrace()
            return listOf(Model(currentModel!!, "bot", Instant.now(), "openai"))
        }
    }

    override fun selectModel(model: String, userName: String): String {
        val models = listModels()
        val selectedModel = models.find { it.id == model }

        if (selectedModel != null) {
            openAiChatOptions.model = selectedModel.id
            return "Model set to ${selectedModel.id}"
        } else {
            return "Model $model not found"
        }
    }
}

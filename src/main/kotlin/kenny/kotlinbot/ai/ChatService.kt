package kenny.kotlinbot.ai

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import org.springframework.web.client.RestClient
import java.time.Instant

interface ChatService {

    fun chat(prompt: String, userName: String): String

    fun imageChat(userName: String, prompt: String, revisedPrompt: String)
    fun randomRole(): String
    fun temperature(temp: Double): String
    fun maxTokens(tokens: Int): String
    fun role(role: String?): String
    fun forget(userName: String): String

    fun listModels(): List<ModelsClient.Model>
    fun selectModel(model: String, userName: String): String
}

class EpochSecondInstantDeserializer : JsonDeserializer<Instant>() {
    override fun deserialize(p: JsonParser, ctx: DeserializationContext): Instant =
        Instant.ofEpochSecond(p.longValue)
}

class ModelsClient(private val rest: RestClient) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    data class Model(
        val id: String,
        @JsonProperty("object") val kind: String,
        @JsonDeserialize(using = EpochSecondInstantDeserializer::class)
        val created: Instant,
        @JsonProperty("owned_by") val ownedBy: String,
    )

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class ListResponse(val data: List<Model>)

    fun listModelIds(): List<Model> =
        rest.get().uri("/models")
            .retrieve()
            .body(ListResponse::class.java)!!
            .data
            .sortedByDescending { it.created }

}
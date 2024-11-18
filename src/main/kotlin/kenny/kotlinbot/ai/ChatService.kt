package kenny.kotlinbot.ai

interface ChatService {

    fun chat(prompt: String, userName: String): String

    fun imageChat(userName: String, prompt: String, revisedPrompt: String)
    fun randomRole(): String
    fun temperature(temp: Double): String
    fun maxTokens(tokens: Int): String
    fun role(role: String?): String
    fun forget(userName: String): String
}
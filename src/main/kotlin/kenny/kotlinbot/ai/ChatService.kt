package kenny.kotlinbot.ai

import org.springframework.ai.chat.messages.Message

interface ChatService {
    fun chat(prompt: String, userName: String): String

    fun randomRole(): String
    fun temperature(temp: Double): String
    fun maxTokens(tokens: Int): String
    fun role(role: String?): String
    fun forget(userName: String): String
}
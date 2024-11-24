package kenny.kotlinbot.storage

interface ChatStorageService {
    fun users(): List<String>
    fun saveUserChats(chats: List<StoredChat>)
    fun getUserChats(userName: String): List<StoredChat>
    fun removeUserChats(userName: String)
}

data class StoredChat(val userName: String, val type: ChatType, val chat: String)

enum class ChatType {
    USER, BOT
}
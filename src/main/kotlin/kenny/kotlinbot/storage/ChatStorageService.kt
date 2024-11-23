package kenny.kotlinbot.storage

interface ChatStorageService {
    fun saveUserChats(userName: String, chats: List<String>)
    fun getUserChats(userName: String): List<String>
}
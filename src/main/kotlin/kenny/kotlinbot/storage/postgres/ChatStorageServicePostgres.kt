package kenny.kotlinbot.storage.postgres

import kenny.kotlinbot.storage.ChatStorageService
import kenny.kotlinbot.storage.StoredChat
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
@Transactional
class ChatStorageServicePostgres(val chatRepository: ChatRepositoryPostgres) : ChatStorageService {
    override fun users(): List<String> {
        return chatRepository.findDistinctUserNames()
    }

    override fun saveUserChats(chats: List<StoredChat>) {
        chats.forEach {
            val chat = Chat(it.userName, it.type, it.chat, LocalDateTime.now())
            chatRepository.save(chat)
            println("Saved: $chat")
        }
    }

    override fun getUserChats(userName: String): List<StoredChat> {
        return chatRepository.getChatsByUserName(userName).map { StoredChat(it.userName!!, it.type!!, it.chat!!) };
    }

    override fun removeUserChats(userName: String) {
        chatRepository.deleteChatsByUserName(userName)
    }
}
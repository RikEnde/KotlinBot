package kenny.kotlinbot.storage.mongo

import kenny.kotlinbot.storage.ChatStorageService
import kenny.kotlinbot.storage.StoredChat
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service

@Profile("mongo")
@Service
class ChatStorageServiceMongo(val chatRepository:  ChatRepositoryMongo) : ChatStorageService {
    override fun users(): List<String> {
        return chatRepository.findDistinctUserNames()
    }

    override fun saveUserChats(chats: List<StoredChat>) {
        chatRepository.saveAll(chats)
    }

    override fun getUserChats(userName: String): List<StoredChat> {
        return chatRepository.findByUserName(userName)
    }

    override fun removeUserChats(userName: String) {
        chatRepository.deleteByUserName(userName)
    }
}
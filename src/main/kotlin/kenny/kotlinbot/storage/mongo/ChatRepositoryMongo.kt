package kenny.kotlinbot.storage.mongo

import kenny.kotlinbot.storage.StoredChat
import org.springframework.context.annotation.Profile
import org.springframework.data.mongodb.repository.Aggregation
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Profile("mongo")
@Repository
interface ChatRepositoryMongo : MongoRepository<StoredChat, String> {

    fun findByUserName(userName: String): List<StoredChat>
    fun deleteByUserName(userName: String)
    fun countByUserName(userName: String): Int

    @Aggregation(pipeline = [
        "{ '\$group': { '_id': '\$userName' } }",
        "{ '\$project': { '_id': 0, 'userName': '\$_id' } }"
    ])
    fun findDistinctUserNames(): List<String>
}
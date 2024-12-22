package kenny.kotlinbot.storage.jpa

import org.springframework.context.annotation.Profile
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Profile("postgres", "h2")
@Repository
interface ChatRepositoryPostgres : JpaRepository<Chat, Long> {
    @Query("SELECT DISTINCT c.userName FROM Chat c")
    fun findDistinctUserNames(): List<String>

    fun getChatsByUserName(userName: String): List<Chat>

    @Modifying(clearAutomatically = true)
    fun deleteChatsByUserName(userName: String): Int
}
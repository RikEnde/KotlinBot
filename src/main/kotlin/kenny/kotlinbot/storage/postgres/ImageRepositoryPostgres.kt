package kenny.kotlinbot.storage.postgres

import org.springframework.context.annotation.Profile
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Profile("postgres")
@Repository
interface ImageRepositoryPostgres : JpaRepository<Image, Long> {
    fun findTop10ByUserNameOrderByCreatedAtDesc(userName: String): List<ImageProjection>

    fun deleteAllByUserName(userName: String)

    fun findByUserNameAndPrompt(userName: String, prompt: String): List<ImageProjection>

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Image i SET i.discordUrl = :discordUrl WHERE i.id = :id")
    fun updateDiscordUrlById(@Param("id") id: Long, @Param("discordUrl") discordUrl: String)
}
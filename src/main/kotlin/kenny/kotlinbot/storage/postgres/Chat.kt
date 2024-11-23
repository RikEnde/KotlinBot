package kenny.kotlinbot.storage.postgres

import jakarta.persistence.*
import org.springframework.data.annotation.Id
import java.time.LocalDateTime

@Entity
@Table(name = "chats", schema = "public")
@Suppress("unused")
class Chat {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "chats_id_seq")
    @SequenceGenerator(
        name = "chats_id_seq", sequenceName = "public.chats_id_seq", allocationSize = 1
    )
    var id: Long? = null

    @Column(name = "user_name", columnDefinition = "TEXT")
    var userName: String? = null

    @Column(name = "chat", columnDefinition = "TEXT")
    var chat: String? = null

    @Column(name = "created_at", insertable = false, updatable = false)
    var createdAt: LocalDateTime? = null

    constructor(
        userName: String?,
        chat: String?,
        createdAt: LocalDateTime?
    ) {
        this.userName = userName
        this.chat = chat
        this.createdAt = createdAt
    }

    override fun toString(): String {
        return "Chat(id=$id, userName=$userName, chat=$chat, createdAt=$createdAt)"
    }
}
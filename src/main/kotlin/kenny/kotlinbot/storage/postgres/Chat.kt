package kenny.kotlinbot.storage.postgres

import jakarta.persistence.*
import kenny.kotlinbot.storage.ChatType
import java.time.LocalDateTime

@Entity
@Table(name = "chats", schema = "public")
@Suppress("unused")
class Chat() {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "images_id_seq")
    @SequenceGenerator(
        name = "images_id_seq", sequenceName = "public.images_id_seq", allocationSize = 1
    )
    var id: Long? = null

    @Column(name = "user_name", columnDefinition = "TEXT")
    var userName: String? = null

    @Column(name = "chat_type")
    var type: ChatType? = null

    @Column(name = "chat", columnDefinition = "TEXT")
    var chat: String? = null

    @Column(name = "created_at", insertable = false, updatable = false)
    var createdAt: LocalDateTime? = null

    constructor(
        userName: String?,
        type: ChatType?,
        chat: String?,
        createdAt: LocalDateTime?
    ) : this() {
        this.userName = userName
        this.type = type
        this.chat = chat
        this.createdAt = createdAt
    }

    override fun toString(): String {
        return "Chat(id=$id, userName=$userName, type=${type}, chat=$chat, createdAt=$createdAt)"
    }
}
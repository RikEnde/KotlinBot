package kenny.kotlinbot.storage.jpa

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "images", schema = "public")
@Suppress("unused")
class Image() : ImageProjection {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "images_id_seq")
    @SequenceGenerator(
        name = "images_id_seq", sequenceName = "public.images_id_seq", allocationSize = 1
    )
    override var id: Long? = null

    @Column(name = "image_data")
    var imageData: ByteArray? = null

    @Column(name = "file_name", columnDefinition = "TEXT")
    override var fileName: String? = null

    @Column(name = "user_name", columnDefinition = "TEXT")
    override var userName: String? = null

    @Column(name = "discord_url", columnDefinition = "TEXT")
    override var discordUrl: String? = null

    @Column(name = "prompt", columnDefinition = "TEXT")
    override var prompt: String? = null

    @Column(name = "revised_prompt", columnDefinition = "TEXT")
    override var revisedPrompt: String? = null

    @Column(name = "created_at", insertable = false, updatable = false)
    override var createdAt: LocalDateTime? = null

    constructor(
        imageData: ByteArray?,
        fileName: String?,
        userName: String?,
        discordUrl: String?,
        prompt: String?,
        revisedPrompt: String?,
        createdAt: LocalDateTime?
    ) : this() {
        this.imageData = imageData
        this.fileName = fileName
        this.userName = userName
        this.discordUrl = discordUrl
        this.prompt = prompt
        this.revisedPrompt = revisedPrompt
        this.createdAt = createdAt
    }
}

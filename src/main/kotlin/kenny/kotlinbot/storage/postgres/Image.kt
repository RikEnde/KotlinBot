package kenny.kotlinbot.storage.postgres

import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.*

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

    @Column(name = "file_name")
    override var fileName: String? = null

    @Column(name = "user_name")
    override var userName: String? = null

    @Column(name = "discord_url")
    override var discordUrl: String? = null

    @Column(name = "prompt")
    override var prompt: String? = null

    @Column(name = "revised_prompt")
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

    override fun toString(): String {
        return "Image(id=$id, imageData=${Arrays.toString(imageData)}, fileName=$fileName, userName=$userName, discordUrl=$discordUrl, prompt=$prompt, revisedPrompt=$revisedPrompt, createdAt=$createdAt)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Image) return false

        return id == other.id && Arrays.equals(
            imageData,
            other.imageData
        ) && fileName == other.fileName && userName == other.userName && discordUrl == other.discordUrl && prompt == other.prompt && revisedPrompt == other.revisedPrompt && createdAt == other.createdAt
    }

    override fun hashCode(): Int {
        var result = id?.hashCode() ?: 0
        result = 31 * result + (imageData?.let { Arrays.hashCode(it) } ?: 0)
        result = 31 * result + (fileName?.hashCode() ?: 0)
        result = 31 * result + (userName?.hashCode() ?: 0)
        result = 31 * result + (discordUrl?.hashCode() ?: 0)
        result = 31 * result + (prompt?.hashCode() ?: 0)
        result = 31 * result + (revisedPrompt?.hashCode() ?: 0)
        result = 31 * result + (createdAt?.hashCode() ?: 0)
        return result
    }
}

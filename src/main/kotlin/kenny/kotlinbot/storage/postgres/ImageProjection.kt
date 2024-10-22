package kenny.kotlinbot.storage.postgres

import java.time.LocalDateTime

interface ImageProjection {
    val id: Long?

    val fileName: String?

    val userName: String?

    val discordUrl: String?

    val prompt: String?

    val revisedPrompt: String?

    val createdAt: LocalDateTime?
}

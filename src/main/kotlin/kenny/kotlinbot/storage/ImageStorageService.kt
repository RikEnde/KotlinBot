package kenny.kotlinbot.storage

import java.io.InputStream
import java.net.URI
import java.net.URL

/**
 * Service for local storage of images
 */
interface ImageStorageService {
    fun store(urlStr: String, userName: String, prompt: String, revisedPrompt: String): StoredImageResult

    fun list(userName: String): List<StoredImageResult>

    fun deleteUserData(userName: String)

    fun findByPrompt(userName: String, prompt: String): List<StoredImageResult>

    fun findById(id: String): StoredImageResult?

    fun load(id: String): InputStream

    fun update(id: String, discordUrl: String)

    companion object {
        fun fileName(url: URL) = url.path.substringAfterLast('/')
        fun url(url: String): URL = URI(url).toURL()
    }
}
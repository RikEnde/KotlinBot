package kenny.kotlinbot.storage

import java.io.BufferedInputStream
import java.io.InputStream
import java.net.URI
import java.net.URL

/**
 * Service for local storage of images
 */
interface StorageService {
    fun store(url: String, userName: String, prompt: String, revisedPrompt: String)

    fun list(userName: String): List<StoredImageResult>

    fun deleteUserData(unitTest: String)

    fun findByPrompt(unitTest: String, prompt: String): List<StoredImageResult>

    fun load(id: String): InputStream

    fun update(id: String, discordUrl: String)
}

object StorageUtils {
    private val filenamePattern = Regex("(?:.*/)?([^/?]+\\.\\w+)(?:\\?.*)?$")

    fun getFilenameFromUrl(url: String): String {
        val matchResult = filenamePattern.find(url)
        return matchResult?.groupValues?.get(1) ?: ""
    }

    fun createURL(urlString: String): URL = URI(urlString).toURL()

    fun openStream(url: URL): InputStream = BufferedInputStream(url.openStream())
}
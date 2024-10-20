package kenny.kotlinbot.storage

import java.io.BufferedInputStream
import java.io.InputStream
import java.net.URI
import java.net.URL
import java.util.regex.Pattern

/**
 * Service for local storage of images
 */
interface StorageService {
    fun store(url: String, userName: String, prompt: String, revisedPrompt: String)

    fun list(userName: String): List<StoredImageResult>

    fun deleteUserData(unitTest: String)

    fun findByPrompt(unitTest: String, prompt: String): List<StoredImageResult>

    fun load(id: String): InputStream?

    fun update(id: String, discordUrl: String)

    fun getFilenameFromUrl(url: String): String {
        val matcher = FILENAME_PATTERN.matcher(url)
        if (matcher.find()) {
            return matcher.group(1)
        }
        return ""
    }

    fun createURL(urlString: String): URL {
        val uri = URI(urlString)
        return uri.toURL()
    }

    fun openStream(url: URL): InputStream {
        return BufferedInputStream(url.openStream())
    }

    companion object {
        val FILENAME_PATTERN: Pattern = Pattern.compile("(?:.*/)?([^/?]+\\.\\w+)(?:\\?.*)?$")
    }
}
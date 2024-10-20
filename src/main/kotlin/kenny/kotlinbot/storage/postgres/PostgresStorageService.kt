package kenny.kotlinbot.storage.postgres

import kenny.kotlinbot.storage.StoredImageResult
import kenny.kotlinbot.storage.StorageService
import java.io.InputStream

class PostgresStorageService : StorageService {
    override fun store(url: String, userName: String, prompt: String, revisedPrompt: String) {
        TODO("Not yet implemented")
    }

    override fun list(userName: String): List<StoredImageResult> {
        TODO("Not yet implemented")
    }

    override fun deleteUserData(unitTest: String) {
        TODO("Not yet implemented")
    }

    override fun findByPrompt(unitTest: String, prompt: String): List<StoredImageResult> {
        TODO("Not yet implemented")
    }

    override fun load(id: String): InputStream? {
        TODO("Not yet implemented")
    }

    override fun update(id: String, discordUrl: String) {
        TODO("Not yet implemented")
    }
}
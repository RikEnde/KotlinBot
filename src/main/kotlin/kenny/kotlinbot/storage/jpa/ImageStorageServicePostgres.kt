package kenny.kotlinbot.storage.jpa

import kenny.kotlinbot.storage.ImageStorageService
import kenny.kotlinbot.storage.ImageStorageService.Companion.fileName
import kenny.kotlinbot.storage.ImageStorageService.Companion.url
import kenny.kotlinbot.storage.StoredImageResult
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import kotlin.jvm.optionals.getOrElse

@Profile("postgres", "h2")
@Service
@Transactional
class ImageStorageServicePostgres(val imageRepository: ImageRepositoryPostgres) : ImageStorageService {
    override fun store(urlStr: String, userName: String, prompt: String, revisedPrompt: String): StoredImageResult {
        val url = url(urlStr)
        val fileName = fileName(url)
        val data = url.openStream().use { it.readBytes() }

        val image = Image(
            imageData = data,
            fileName = fileName,
            userName = userName,
            discordUrl = null,
            prompt = prompt,
            revisedPrompt = revisedPrompt,
            createdAt = LocalDateTime.ofInstant(Instant.now(), ZoneId.of("UTC"))
        )

        val savedImage = imageRepository.save(image)
        return imageResultMapper(savedImage)
    }

    val imageResultMapper: (ImageProjection) -> StoredImageResult = { f ->
        StoredImageResult(
            f.id.toString(),
            f.fileName.toString(),
            StoredImageResult.MetaData(
                f.userName.toString(),
                f.discordUrl,
                f.prompt.toString(),
                f.revisedPrompt.toString()
            )
        )
    }

    override fun list(userName: String): List<StoredImageResult> =
        imageRepository.findTop10ByUserNameOrderByCreatedAtDesc(userName).map(imageResultMapper)

    override fun deleteUserData(userName: String) = imageRepository.deleteAllByUserName(userName)

    override fun findByPrompt(userName: String, prompt: String): List<StoredImageResult> =
        imageRepository.findByUserNameAndPrompt(userName, prompt).map(imageResultMapper)

    override fun findById(id: String): StoredImageResult? =
        imageRepository.findById(id.toLong()).map { imageResultMapper(it) }.orElse(null)

    override fun load(id: String): InputStream {
        val image = imageRepository.findById(id.toLong()).getOrElse {
            throw RuntimeException("Not found id $id")
        }
        return ByteArrayInputStream(image.imageData)
    }

    override fun update(id: String, discordUrl: String) =
        imageRepository.updateDiscordUrlById(id.toLong(), discordUrl)
}
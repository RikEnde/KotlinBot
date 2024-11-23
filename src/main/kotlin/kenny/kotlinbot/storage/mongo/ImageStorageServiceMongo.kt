package kenny.kotlinbot.storage.mongo

import com.mongodb.client.gridfs.model.GridFSFile
import kenny.kotlinbot.storage.ImageStorageService
import kenny.kotlinbot.storage.ImageStorageService.Companion.fileName
import kenny.kotlinbot.storage.ImageStorageService.Companion.url
import kenny.kotlinbot.storage.StoredImageResult
import org.springframework.context.annotation.Profile
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria.where
import org.springframework.data.mongodb.core.query.Query.query
import org.springframework.data.mongodb.gridfs.GridFsTemplate
import org.springframework.stereotype.Service
import java.io.InputStream

@Profile("mongo")
@Service
class ImageStorageServiceMongo(val gridFsTemplate: GridFsTemplate, val mongoTemplate: MongoTemplate) : ImageStorageService {
    val mapper = { f: GridFSFile ->
        StoredImageResult(
            f.objectId.toHexString(),
            f.filename,
            StoredImageResult.MetaData(
                f.metadata?.getString("userName").orEmpty(),
                f.metadata?.getString("discordUrl").orEmpty(),
                f.metadata?.getString("prompt").orEmpty(),
                f.metadata?.getString("revisedPrompt").orEmpty()
            )
        )
    }

    override fun store(urlStr: String, userName: String, prompt: String, revisedPrompt: String): StoredImageResult {
        val url = url(urlStr)
        val fileName: String = fileName(url)
        val metaData = StoredImageResult.MetaData(userName, null, prompt, revisedPrompt)

        url.openStream().use {
            val id = gridFsTemplate.store(it, fileName, metaData)
            return StoredImageResult(id.toString(), fileName, metaData)
        }
    }

    override fun list(userName: String): List<StoredImageResult> {
        return gridFsTemplate.find(
            query(where("metadata.userName").`is`(userName)).with(Sort.by(Sort.Direction.DESC, "uploadDate")).limit(10)
        ).map(mapper).toList()
    }

    override fun deleteUserData(userName: String) {
        gridFsTemplate.delete(query(where("metadata.userName").`is`(userName)))
    }

    override fun findByPrompt(userName: String, prompt: String): List<StoredImageResult> {
        return gridFsTemplate.find(
            query(where("metadata.userName").`is`(userName).and("metadata.prompt").`is`(prompt))
        ).map(mapper).toList()
    }

    override fun findById(id: String): StoredImageResult? {
        return mapper(gridFsTemplate.findOne(query(where("_id").`is`(id))))
    }

    override fun load(id: String): InputStream {
        val result = gridFsTemplate.findOne(query(where("_id").`is`(id)))
        return gridFsTemplate.getResource(result).inputStream
    }

    override fun update(id: String, discordUrl: String) {
        gridFsTemplate.findOne(query(where("_id").`is`(id))).let { file ->
            file.metadata?.apply {
                this["discordUrl"] = discordUrl
                mongoTemplate.save(file, "fs.files")
            }
        }
    }
}

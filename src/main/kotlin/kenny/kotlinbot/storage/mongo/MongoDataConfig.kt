package kenny.kotlinbot.storage.mongo

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories

@Configuration
@Profile("mongo")
@EnableMongoRepositories(basePackages = ["kenny.kotlinbot.storage.mongo"])
class MongoDataConfig : AbstractMongoClientConfiguration() {
    @Value("\${spring.data.mongodb.database}")
    lateinit var db: String
    override fun getDatabaseName(): String {
        return db
    }
}
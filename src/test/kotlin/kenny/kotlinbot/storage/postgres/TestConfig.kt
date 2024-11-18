package kenny.kotlinbot.storage.postgres

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration
import org.springframework.boot.autoconfigure.data.mongo.MongoRepositoriesAutoConfiguration
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration
import org.springframework.test.context.TestPropertySource

@SpringBootApplication(
    exclude = [
        MongoDataAutoConfiguration::class,
        MongoAutoConfiguration::class,
        MongoRepositoriesAutoConfiguration::class
    ],
    scanBasePackages = ["kenny.kotlinbot.storage.postgres"]
)
@TestPropertySource(properties = ["spring.jpa.hibernate.ddl-auto=create"])
class TestConfig
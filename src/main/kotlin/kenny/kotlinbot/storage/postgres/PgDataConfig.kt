package kenny.kotlinbot.storage.postgres

import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@Configuration
@Profile("postgres", "openai")
@EnableJpaRepositories(basePackages = ["kenny.kotlinbot.storage.postgres"])
class PgDataConfig
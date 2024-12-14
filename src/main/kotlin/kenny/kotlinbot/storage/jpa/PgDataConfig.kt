package kenny.kotlinbot.storage.jpa

import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@Configuration
@Profile(value = ["postgres", "h2"])
@EnableJpaRepositories(basePackages = ["kenny.kotlinbot.storage.jpa"])
class PgDataConfig
package kenny.kotlinbot

import org.slf4j.LoggerFactory
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.WebApplicationType
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class KotlinBotApplication : CommandLineRunner {

    private val log = LoggerFactory.getLogger(KotlinBotApplication::class.java)

    override fun run(vararg args: String?) {
        args.forEach { log.info("Application args: $it") }
    }
}

fun main(args: Array<String>) {
    runApplication<KotlinBotApplication>(*args) {
        setWebApplicationType(WebApplicationType.NONE)
    }
}


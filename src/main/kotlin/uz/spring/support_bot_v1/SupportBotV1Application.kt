package uz.spring.support_bot_v1

import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.info.Contact
import io.swagger.v3.oas.annotations.info.Info
import lombok.extern.slf4j.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.event.ContextRefreshedEvent
import org.springframework.context.event.EventListener
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.stereotype.Component
import org.telegram.telegrambots.bots.DefaultBotOptions
import org.telegram.telegrambots.meta.TelegramBotsApi
import org.telegram.telegrambots.meta.exceptions.TelegramApiException
import org.telegram.telegrambots.meta.generics.TelegramBot
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession


@SpringBootApplication
@EnableJpaRepositories(repositoryBaseClass = BaseRepositoryImpl::class)
@EnableConfigurationProperties(TelegramBotProperties::class)
@OpenAPIDefinition(
    info = Info(
        title = "Support bot",
        version = "1.0",
        description = "",
        contact = Contact(name = "G1", email = "jaloliddindeveloper@gmail.com")
    )
)
class SupportBotV1Application

@Bean
fun defaultBotOptions(): DefaultBotOptions {
    val options = DefaultBotOptions()
    options.maxThreads = 10
    return options
}


@Component
@Slf4j
@ComponentScan("uz.spring.support_bot_v1")
class BotInitializer(private val bot: TgBotController) {

    @EventListener(ContextRefreshedEvent::class)
    fun init() {
        val api = TelegramBotsApi(DefaultBotSession::class.java)
        try {
            api.registerBot(bot)
        } catch (e: TelegramApiException) {
            e.printStackTrace()
        }
    }
}

fun main(args: Array<String>) {
    runApplication<SupportBotV1Application>(*args)
}


package uz.spring.support_bot_v1

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.MessageSource
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.telegram.telegrambots.meta.TelegramBotsApi
import org.telegram.telegrambots.meta.exceptions.TelegramApiException
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession

@SpringBootApplication
@EnableJpaRepositories(repositoryBaseClass = BaseRepositoryImpl::class)
class SupportBotV1Application

val log: Logger = LoggerFactory.getLogger("main")

fun main(args: Array<String>) {

    val botSession = DefaultBotSession()

    runApplication<SupportBotV1Application>(*args)
    while (true) {
        val token = token1
        val telegramBotsApi = TelegramBotsApi(botSession::class.java).registerBot(
            TgBotController(
                botService = TgBotService(),
                token = token
            )
        )

        try {
            botSession.stop()
            telegramBotsApi.stop()
            log.info("Starting the bot ...")
            botSession.start()
            telegramBotsApi.start()
        } catch (e: TelegramApiException) {
            log.error("Telegram API failure", e)
        }
    }
}

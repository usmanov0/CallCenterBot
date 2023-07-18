package uz.spring.support_bot_v1

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.TelegramBotsApi
import org.telegram.telegrambots.meta.exceptions.TelegramApiException
import org.telegram.telegrambots.meta.generics.TelegramBot
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession


@SpringBootApplication
@EnableJpaRepositories(repositoryBaseClass = BaseRepositoryImpl::class)
class SupportBotV1Application


@Component
class BotInitializer : CommandLineRunner {

    @Autowired
    private lateinit var telegramBot: TgBotController

    override fun run(vararg args: String?) {
        val botSession = DefaultBotSession()
        val telegramBotsApi = TelegramBotsApi(botSession::class.java)
        try {
            telegramBotsApi.registerBot(telegramBot)
        } catch (e: TelegramApiException) {
            e.printStackTrace()
        }
    }
}

fun main(args: Array<String>) {


    runApplication<SupportBotV1Application>(*args)
}


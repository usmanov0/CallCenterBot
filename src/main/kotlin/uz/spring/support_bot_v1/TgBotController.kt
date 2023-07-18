package uz.spring.support_bot_v1

import org.springframework.stereotype.Component
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.objects.Update

@Component
class TgBotController(
    private val botService: TgBotService
) : TelegramLongPollingBot() {
    override fun getBotUsername(): String {
        return username
    }

    @Deprecated("Deprecated in Java", ReplaceWith("token"))
    override fun getBotToken(): String {
        return token1
    }

    override fun onUpdateReceived(update: Update) {
        if (update.hasMessage()) {
            val message = update.message
            when (message.text) {
                "/start" -> {
                    execute(botService.start(message))
                }
                UZBEK, ENGLISH, RUSSIAN -> {
                    execute(botService.chooseLanguage(message))
                }
            }
        }
    }
}
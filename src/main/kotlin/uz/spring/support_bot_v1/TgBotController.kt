package uz.spring.support_bot_v1

import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.objects.Update

class TgBotController(
    private val botService: TgBotService,
    private val token: String
) : TelegramLongPollingBot() {
    override fun getBotUsername(): String {
        return username
    }

    @Deprecated("Deprecated in Java")
    override fun getBotToken(): String {
        return token
    }

    override fun onUpdateReceived(update: Update) {
        if (update.hasMessage()) {
            val message = update.message
            when (message.text) {
                "/start" -> {
                    execute(botService.start(message))
                }
                UZBEK, ENGLISH, RUSSIAN -> {
                    execute(botService.language(message))
                }
            }
        }
    }
}
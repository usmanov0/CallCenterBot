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
        val message = update.message
        if (update.hasMessage()) {
            if (update.message.hasContact()) {
                execute(botService.shareContact(message))
            } else {
                when (message.text) {
                    START -> {
                        execute(botService.start(message))
                    }
                    UZBEK_, ENGLISH_, RUSSIAN_ -> {
                        execute(botService.chooseLanguage(message))
                    }
                    BACK_UZ, BACK_EN, BACK_RU -> {
                        execute(botService.back(message))
                    }
                }
            }


        }
    }
}
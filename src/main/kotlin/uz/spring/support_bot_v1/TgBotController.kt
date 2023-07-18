package uz.spring.support_bot_v1

import org.springframework.stereotype.Component
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.objects.Update


@Component
class TgBotController(
    private val telegramBotProperties: TelegramBotProperties,
    private val messageHandler: MessageHandler,
    private val callbackQueryHandler: CallbackQueryHandler,
) : TelegramLongPollingBot(telegramBotProperties.token) {

    override fun getBotUsername(): String {
        return telegramBotProperties.username
    }

    override fun onUpdateReceived(update: Update) {
        when {
            update.hasCallbackQuery() -> callbackQueryHandler.handle(update.callbackQuery, this)
            update.hasMessage() -> messageHandler.handle(update.message, this)
        }

    }
}
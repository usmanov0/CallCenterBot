package uz.spring.support_bot_v1

import org.springframework.context.MessageSource
import org.springframework.stereotype.Service
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.User
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow
import java.util.*
import kotlin.collections.ArrayList

@Service
class TgBotService(
    private val messageSource: MessageSource,
    private val userRepository: UserRepository,
) {

    private fun registerUser(tgUser: User): Users {
        return userRepository.findByChatId(tgUser.id)
            ?: return userRepository.save(
                Users(
                    tgUser.firstName,
                    tgUser.lastName,
                    "",
                    tgUser.id,
                    USER,
                    true,
                    SHARE_CONTACT,
                    null
                )
            )
    }

    fun start(message: Message): SendMessage {
        val sendMessage = SendMessage()
        sendMessage.chatId = "${message.chatId}"
        val firstName = message.chat.firstName
//        sendMessage.replyMarkup = ReplyKeyboardRemove(true)


        sendMessage.text =
            """
                        Assalomu alaykum ${firstName}. Men Support botman!
                        Привет ${firstName}! Men Support botman!
                        Hi ${firstName}! Men Support botman!
                        
                        Muloqot tilini tanlang
                        Выберите язык
                        Select Language
                        """.trimIndent()

        println("${message.chatId}")

        val keyboardMarkup = ReplyKeyboardMarkup()
        val keyboard: MutableList<KeyboardRow> = ArrayList()
        val row = KeyboardRow()

        row.add(UZBEK)
        row.add(RUSSIAN)
        row.add(ENGLISH)

        keyboard.add(row)
        keyboardMarkup.keyboard = keyboard
        keyboardMarkup.resizeKeyboard = true
        sendMessage.replyMarkup = keyboardMarkup

        return sendMessage
    }

    fun chooseLanguage(message: Message): SendMessage {
        val sendMessage = SendMessage()
        sendMessage.chatId = "${message.chatId}"
        val text = message.text
        val registerUser = registerUser(message.from)

        val language = when (text) {
            "uzbek" -> LanguageEnum.UZBEK
            "russian" -> LanguageEnum.RUSSIAN
            "english" -> LanguageEnum.ENGLISH
            else -> LanguageEnum.UZBEK
        }
        registerUser.language = language

        userRepository.save(registerUser)
//        messageSource.getMessage(LANGUAGE, arrayOf(String()), Locale.forLanguageTag(registerUser.language.toString()))
        when (registerUser.language!!.ll) {
            "uz" -> {
                sendMessage.text = "Marhamat savol berishinggiz mumkin !"
                val keyboardMarkup = ReplyKeyboardMarkup()
                val keyboard: MutableList<KeyboardRow> = ArrayList()
                val row = KeyboardRow()

                row.add("Savol berish")
                row.add("Ortga")

                keyboard.add(row)
                keyboardMarkup.keyboard = keyboard
                keyboardMarkup.resizeKeyboard = true
                sendMessage.replyMarkup = keyboardMarkup
            }

            "ru" -> {
                sendMessage.text = "Пожалуйста, не стесняйтесь задавать вопросы !"
                val keyboardMarkup = ReplyKeyboardMarkup()
                val keyboard: MutableList<KeyboardRow> = ArrayList()
                val row = KeyboardRow()

                row.add("Задайте вопрос")
                row.add("Назад")

                keyboard.add(row)
                keyboardMarkup.keyboard = keyboard
                keyboardMarkup.resizeKeyboard = true
                sendMessage.replyMarkup = keyboardMarkup
            }

            "en" -> {
                sendMessage.text = "Please feel free to ask a question !"
                val keyboardMarkup = ReplyKeyboardMarkup()
                val keyboard: MutableList<KeyboardRow> = ArrayList()
                val row = KeyboardRow()

                row.add("Ask a question")
                row.add("Back")

                keyboard.add(row)
                keyboardMarkup.keyboard = keyboard
                keyboardMarkup.resizeKeyboard = true
                sendMessage.replyMarkup = keyboardMarkup
            }
        }
        return sendMessage

    }
}


/* command.startsWith("/teams") -> {
               sendMessage.text = "We have 3 main vectors: Utilihive, Cloudwheel and Consultancy"

               val keyboardMarkup = ReplyKeyboardMarkup()
               val keyboard: MutableList<KeyboardRow> = ArrayList()
               val row = KeyboardRow()
               row.add("Utilihive")
               row.add("Cloudwheel")
               row.add("Consultancy")

               keyboard.add(row)
               keyboardMarkup.keyboard = keyboard
               sendMessage.replyMarkup = keyboardMarkup
           }*/
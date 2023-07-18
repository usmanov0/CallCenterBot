package uz.spring.support_bot_v1

import org.springframework.stereotype.Service
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.CallbackQuery
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.User
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow
import org.telegram.telegrambots.meta.bots.AbsSender

interface MessageHandler {
    fun handle(message: Message, sender: AbsSender)
}

interface CallbackQueryHandler {
    fun handle(callbackQuery: CallbackQuery, sender: AbsSender)
}

@Service
class MessageHandlerImpl(
    private val userRepository: UserRepository
) : MessageHandler {
    private fun registerUser(tgUser: User): Users {
        return userRepository.findByAccountId(tgUser.id)
            ?: return userRepository.save(
                Users(
                    tgUser.firstName,
                    tgUser.lastName,
                    "",
                    tgUser.id,
                    USER,
                    true,
                    CHOOSE_LANGUAGE,
                    null
                )
            )
    }

    private fun start(message: Message, sender: AbsSender) {
        val sendMessage = SendMessage()
        sendMessage.chatId = message.chatId.toString()
        val firstName = message.chat.firstName
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

        row.add(UZBEK_)
        row.add(RUSSIAN_)
        row.add(ENGLISH_)

        keyboard.add(row)
        keyboardMarkup.keyboard = keyboard
        keyboardMarkup.resizeKeyboard = true
        sendMessage.replyMarkup = keyboardMarkup

        sender.execute(sendMessage)
    }

    private fun chooseLanguage(message: Message, sender: AbsSender) {
        val sendMessage = SendMessage()
        sendMessage.chatId = message.chatId.toString()

        val registerUser = registerUser(message.from)

        val language = when (message.text) {
            UZBEK_ -> LanguageEnum.Uzbek
            RUSSIAN_ -> LanguageEnum.Russian
            ENGLISH_ -> LanguageEnum.English
            else -> LanguageEnum.Uzbek
        }
        registerUser.state = SHARE_CONTACT
        registerUser.language = mutableSetOf(language)
        userRepository.save(registerUser)

        val language1 = registerUser.language!!.first()

        when (language1.toString()) {
            UZBEK -> {
                sendMessage.text = SHARE_CONTACT_UZ

                val shareContactButton = KeyboardButton()
                shareContactButton.text = SHARE_UZ
                shareContactButton.requestContact = true

                val keyboardMarkup = ReplyKeyboardMarkup()
                val keyboard: MutableList<KeyboardRow> = ArrayList()
                val row = KeyboardRow()
                val row1 = KeyboardRow()

                row.add(shareContactButton)
                row1.add(BACK_UZ)

                keyboard.add(row)
                keyboard.add(row1)
                keyboardMarkup.keyboard = keyboard
                keyboardMarkup.resizeKeyboard = true
                sendMessage.replyMarkup = keyboardMarkup
            }

            RUSSIAN -> {
                sendMessage.text = SHARE_CONTACT_RU

                val shareContactButton = KeyboardButton()
                shareContactButton.text = SHARE_RU
                shareContactButton.requestContact = true

                val keyboardMarkup = ReplyKeyboardMarkup()
                val keyboard: MutableList<KeyboardRow> = ArrayList()

                val row = KeyboardRow()
                val row1 = KeyboardRow()

                row.add(shareContactButton)
                row1.add(BACK_RU)

                keyboard.add(row)
                keyboard.add(row1)
                keyboardMarkup.keyboard = keyboard
                keyboardMarkup.resizeKeyboard = true
                sendMessage.replyMarkup = keyboardMarkup
            }

            ENGLISH -> {
                sendMessage.text = SHARE_CONTACT_EN

                val shareContactButton = KeyboardButton()
                shareContactButton.text = SHARE_EN
                shareContactButton.requestContact = true

                val keyboardMarkup = ReplyKeyboardMarkup()
                val keyboard: MutableList<KeyboardRow> = ArrayList()
                val row = KeyboardRow()
                val row1 = KeyboardRow()

                row.add(shareContactButton)
                row1.add(BACK_EN)

                keyboard.add(row)
                keyboard.add(row1)
                keyboardMarkup.keyboard = keyboard
                keyboardMarkup.resizeKeyboard = true
                sendMessage.replyMarkup = keyboardMarkup
            }
        }
        sender.execute(sendMessage)
    }

    private fun back(message: Message,sender: AbsSender) {
        val registerUser = registerUser(message.from)
        return when (registerUser.state) {
            SHARE_CONTACT -> start(message,sender)
            QUESTION -> chooseLanguage(message,sender)
            else -> start(message,sender)
        }
    }

    override fun handle(message: Message, sender: AbsSender) {
        val text = message.text
        val telegramUser = message.from
        val chatId = telegramUser.id.toString()

        val sendMessage = SendMessage()
        sendMessage.enableHtml(true)
        sendMessage.chatId = chatId

        if (message.hasText()) {
            when (text) {
                START -> start(message, sender)

                UZBEK_, RUSSIAN_, ENGLISH_ -> chooseLanguage(message, sender)

                BACK_UZ, BACK_RU, BACK_EN -> back(message, sender)

                else -> {

                }
            }
        } else if (message.hasContact()) {

            val phone = message.contact.phoneNumber
            val registerUser = registerUser(message.from)

            registerUser.state = QUESTION
            registerUser.phone = phone.toString()
            userRepository.save(registerUser)

            val language1 = registerUser.language!!.first()

            when (language1.toString()) {
                UZBEK -> {
                    sendMessage.text = QUESTION_UZ
                    val keyboardMarkup = ReplyKeyboardMarkup()
                    val keyboard: MutableList<KeyboardRow> = ArrayList()
                    val row = KeyboardRow()
                    val row1 = KeyboardRow()

                    row.add(SEND_QUESTION_UZ)
                    row1.add(BACK_UZ)

                    keyboard.add(row)
                    keyboard.add(row1)
                    keyboardMarkup.keyboard = keyboard
                    keyboardMarkup.resizeKeyboard = true
                    sendMessage.replyMarkup = keyboardMarkup
                }

                RUSSIAN -> {
                    sendMessage.text = QUESTION_RU
                    val keyboardMarkup = ReplyKeyboardMarkup()
                    val keyboard: MutableList<KeyboardRow> = ArrayList()
                    val row = KeyboardRow()
                    val row1 = KeyboardRow()

                    row.add(SEND_QUESTION_RU)
                    row1.add(BACK_RU)

                    keyboard.add(row)
                    keyboard.add(row1)
                    keyboardMarkup.keyboard = keyboard
                    keyboardMarkup.resizeKeyboard = true
                    sendMessage.replyMarkup = keyboardMarkup
                }

                ENGLISH -> {
                    sendMessage.text = QUESTION_EN
                    val keyboardMarkup = ReplyKeyboardMarkup()
                    val keyboard: MutableList<KeyboardRow> = ArrayList()
                    val row = KeyboardRow()
                    val row1 = KeyboardRow()

                    row.add(SEND_QUESTION_EN)
                    row1.add(BACK_EN)

                    keyboard.add(row)
                    keyboard.add(row1)
                    keyboardMarkup.keyboard = keyboard
                    keyboardMarkup.resizeKeyboard = true
                    sendMessage.replyMarkup = keyboardMarkup
                }
            }
//        sendMessage.replyMarkup = ReplyKeyboardRemove(true)
            sender.execute(sendMessage)
        }


    }
}

@Service
class CallbackQueryHandlerImpl : CallbackQueryHandler {
    override fun handle(callbackQuery: CallbackQuery, sender: AbsSender) {
        val text = callbackQuery.data
        val telegramUser = callbackQuery.from
        val chatId = telegramUser.id.toString()

        val sendMessage = SendMessage()
        sendMessage.enableHtml(true)
        sendMessage.chatId = chatId

        if (callbackQuery.message.hasText())
            when (text) {
                "/start" -> {
                    sendMessage.text = "<b>Salom</b> ${telegramUser.userName}"
                    sender.execute(sendMessage)
                }

                else -> {

                }
            }

    }

}


/*
@Service
class TgBotService(
    private val messageRepository: MessageRepository,
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
                    CHOOSE_LANGUAGE,
                    null
                )
            )
    }

//    private fun registerMessage(message: Message): MyMessage {
//        return messageRepository.findby(tgUser.id)
//            ?: return userRepository.save(
//                Users(
//                    tgUser.firstName,
//                    tgUser.lastName,
//                    "",
//                    tgUser.id,
//                    USER,
//                    true,
//                    CHOOSE_LANGUAGE,
//                    null
//                )
//            )
//    }

    fun start(message: Message): SendMessage {
        val sendMessage = SendMessage()
        sendMessage.chatId = "${message.chatId}"
        val firstName = message.chat.firstName
        sendMessage.replyMarkup = ReplyKeyboardRemove(true)


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

        row.add(UZBEK_)
        row.add(RUSSIAN_)
        row.add(ENGLISH_)

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
            UZBEK_ -> LanguageEnum.Uzbek
            RUSSIAN_ -> LanguageEnum.Russian
            ENGLISH_ -> LanguageEnum.English
            else -> LanguageEnum.Uzbek
        }
        registerUser.state = SHARE_CONTACT
        registerUser.language = mutableSetOf(language)
        userRepository.save(registerUser)

        val language1 = registerUser.language!!.first()

        when (language1.toString()) {
            UZBEK -> {
                sendMessage.text = SHARE_CONTACT_UZ

                val shareContactButton = KeyboardButton()
                shareContactButton.text = SHARE_UZ
                shareContactButton.requestContact = true

                val keyboardMarkup = ReplyKeyboardMarkup()
                val keyboard: MutableList<KeyboardRow> = ArrayList()
                val row = KeyboardRow()
                val row1 = KeyboardRow()

                row.add(shareContactButton)
                row1.add(BACK_UZ)

                keyboard.add(row)
                keyboard.add(row1)
                keyboardMarkup.keyboard = keyboard
                keyboardMarkup.resizeKeyboard = true
                sendMessage.replyMarkup = keyboardMarkup
            }

            RUSSIAN -> {
                sendMessage.text = SHARE_CONTACT_RU

                val shareContactButton = KeyboardButton()
                shareContactButton.text = SHARE_RU
                shareContactButton.requestContact = true

                val keyboardMarkup = ReplyKeyboardMarkup()
                val keyboard: MutableList<KeyboardRow> = ArrayList()

                val row = KeyboardRow()
                val row1 = KeyboardRow()

                row.add(shareContactButton)
                row1.add(BACK_RU)

                keyboard.add(row)
                keyboard.add(row1)
                keyboardMarkup.keyboard = keyboard
                keyboardMarkup.resizeKeyboard = true
                sendMessage.replyMarkup = keyboardMarkup
            }

            ENGLISH -> {
                sendMessage.text = SHARE_CONTACT_EN

                val shareContactButton = KeyboardButton()
                shareContactButton.text = SHARE_EN
                shareContactButton.requestContact = true

                val keyboardMarkup = ReplyKeyboardMarkup()
                val keyboard: MutableList<KeyboardRow> = ArrayList()
                val row = KeyboardRow()
                val row1 = KeyboardRow()

                row.add(shareContactButton)
                row1.add(BACK_EN)

                keyboard.add(row)
                keyboard.add(row1)
                keyboardMarkup.keyboard = keyboard
                keyboardMarkup.resizeKeyboard = true
                sendMessage.replyMarkup = keyboardMarkup
            }
        }
        return sendMessage

    }

    fun shareContact(message: Message): SendMessage {
        val sendMessage = SendMessage()
        sendMessage.chatId = "${message.chatId}"
        val text = message.contact.phoneNumber
        val registerUser = registerUser(message.from)

        registerUser.state = QUESTION
        registerUser.phone = text.toString()
        userRepository.save(registerUser)

        val language1 = registerUser.language!!.first()

        when (language1.toString()) {
            UZBEK -> {
                sendMessage.text = QUESTION_UZ
                val keyboardMarkup = ReplyKeyboardMarkup()
                val keyboard: MutableList<KeyboardRow> = ArrayList()
                val row = KeyboardRow()
                val row1 = KeyboardRow()

                row.add(SEND_QUESTION_UZ)
                row1.add(BACK_UZ)

                keyboard.add(row)
                keyboard.add(row1)
                keyboardMarkup.keyboard = keyboard
                keyboardMarkup.resizeKeyboard = true
                sendMessage.replyMarkup = keyboardMarkup
            }

            RUSSIAN -> {
                sendMessage.text = QUESTION_RU
                val keyboardMarkup = ReplyKeyboardMarkup()
                val keyboard: MutableList<KeyboardRow> = ArrayList()
                val row = KeyboardRow()
                val row1 = KeyboardRow()

                row.add(SEND_QUESTION_RU)
                row1.add(BACK_RU)

                keyboard.add(row)
                keyboard.add(row1)
                keyboardMarkup.keyboard = keyboard
                keyboardMarkup.resizeKeyboard = true
                sendMessage.replyMarkup = keyboardMarkup
            }

            ENGLISH -> {
                sendMessage.text = QUESTION_EN
                val keyboardMarkup = ReplyKeyboardMarkup()
                val keyboard: MutableList<KeyboardRow> = ArrayList()
                val row = KeyboardRow()
                val row1 = KeyboardRow()

                row.add(SEND_QUESTION_EN)
                row1.add(BACK_EN)

                keyboard.add(row)
                keyboard.add(row1)
                keyboardMarkup.keyboard = keyboard
                keyboardMarkup.resizeKeyboard = true
                sendMessage.replyMarkup = keyboardMarkup
            }
        }
//        sendMessage.replyMarkup = ReplyKeyboardRemove(true)
        return sendMessage

    }

//    fun question(message: Message):SendMessage{
//        val sendMessage = SendMessage()
//        sendMessage.chatId = "${message.chatId}"
//        val registerUser = registerUser(message.from)
//
//    }

    fun back(message: Message): SendMessage {
        val sendMessage = SendMessage()
        sendMessage.chatId = "${message.chatId}"
        val registerUser = registerUser(message.from)
        return when (registerUser.state) {
            SHARE_CONTACT -> start(message)
            QUESTION -> chooseLanguage(message)
            else -> start(message)
        }
    }
}
*/

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
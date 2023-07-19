package uz.spring.support_bot_v1

import org.springframework.stereotype.Service
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.CallbackQuery
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.User
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow
import org.telegram.telegrambots.meta.bots.AbsSender

interface MessageHandler {
    fun handle(message: Message, sender: AbsSender)
}

interface CallbackQueryHandler {   // baholash
    fun handle(callbackQuery: CallbackQuery, sender: AbsSender)
}

@Service
class MessageHandlerImpl(
    private val userRepository: UserRepository,
    private val messageRepository: MessageRepository,
    private val messageService: MessageService,
    private val sessionRepository: SessionRepository
) : MessageHandler {

    private fun registerUser(tgUser: User): Users {
        return userRepository.findByChatIdAndDeletedFalse(tgUser.id)
            ?: return userRepository.save(
                Users(
                    null,
                    tgUser.id,
                    Role.USER,
                    null,
                    null,
                    true,
                    CHOOSE_LANGUAGE,
                    tgUser.lastName,
                    tgUser.firstName
                )
            )
    }

    private fun start(message: Message, sender: AbsSender) {
        val chatId = message.from.id
        val user = userRepository.findByChatIdAndDeletedFalse(chatId)
        if (user == null || user.role == Role.USER) {
            startUser(message, sender)
        } else {
            startOperator(message, sender)
        }

    }

    private fun startUser(message: Message, sender: AbsSender) {

        val user = message.from
        val registerUser = registerUser(user)

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

    private fun startOperator(message: Message, sender: AbsSender) {
        val sendMessage = SendMessage()
        sendMessage.chatId = message.chatId.toString()
        sendMessage.text = "Ishni boshlash uchun Online tugmasini bosing !"

        val keyboardMarkup = ReplyKeyboardMarkup()
        val keyboard: MutableList<KeyboardRow> = ArrayList()
        val row = KeyboardRow()

        row.add(ONLINE_UZ)

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
        if (registerUser.phone != null)
            sendQuestion(message, sender)
        else {
            registerUser.state = SHARE_CONTACT
            registerUser.language = mutableSetOf(language)
            userRepository.save(registerUser)

            val language1 = registerUser.language!!.first()  //   [Russian]

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

    }

    private fun sendQuestion(message: Message, sender: AbsSender) {
        val sendMessage = SendMessage()
        sendMessage.chatId = message.chatId.toString()

        val registerUser = registerUser(message.from)

        registerUser.state = SEND_QUESTION

        userRepository.save(registerUser)

        val language1 = registerUser.language!!.first()  //   [Russian]

        when (language1.toString()) {
            UZBEK -> {
                sendMessage.text = SEND_QUESTION_TRUE_UZ
                sendMessage.replyMarkup = ReplyKeyboardRemove(true)

            }

            RUSSIAN -> {
                sendMessage.text = SEND_QUESTION_TRUE_RU
                sendMessage.replyMarkup = ReplyKeyboardRemove(true)

            }

            ENGLISH -> {
                sendMessage.text = SEND_QUESTION_TRUE_EN
                sendMessage.replyMarkup = ReplyKeyboardRemove(true)

            }
        }
        sender.execute(sendMessage)
    }

    private fun sendNextQuestions(message: Message, sender: AbsSender) {
        val sendMessage = SendMessage()
        sendMessage.chatId = message.from.id.toString()
        val registerUser = registerUser(message.from)

        registerUser.state = WAITING

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

    private fun handleQuestion(message: Message, sender: AbsSender) {
        val sendMessage = SendMessage()
        sendMessage.chatId = message.chatId.toString()

        val registerUser = registerUser(message.from)

        registerUser.state = WAITING  //CHANGE

        userRepository.save(registerUser)

        val language1 = registerUser.language!!.first()  //   [Russian]

        when (language1.toString()) {
            UZBEK -> {
                messageService.userWriteMsg(
                    UserMessageDto(
                        message.text,
                        registerUser.chatId,
                        registerUser.language!!.first().name
                    )
                )
                sendMessage.text = SENDING_OPERATOR_UZ
            }

            RUSSIAN -> {
                messageService.userWriteMsg(
                    UserMessageDto(
                        message.text,
                        registerUser.chatId,
                        registerUser.language!!.first().name
                    )
                )
                sendMessage.text = SENDING_OPERATOR_RU
            }

            ENGLISH -> {
                messageService.userWriteMsg(
                    UserMessageDto(
                        message.text,
                        registerUser.chatId,
                        registerUser.language!!.first().name
                    )
                )
                sendMessage.text = SENDING_OPERATOR_EN
            }
        }
        sender.execute(sendMessage)
        sendNextQuestions(message, sender)
    }

    private fun getQuestions(message: Message, sender: AbsSender) {
        val sendMessage = SendMessage()
        sendMessage.chatId = message.from.id.toString()

        val registerUser = registerUser(message.from)
        /*

                when (registerUser.language!!.first().toString()) {
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
        */

//        registerUser.state = WAITING  //CHANGE
//
//        userRepository.save(registerUser)

        val keyboardMarkup = ReplyKeyboardMarkup()
        val keyboard: MutableList<KeyboardRow> = ArrayList()
        val row = KeyboardRow()
        val row1 = KeyboardRow()

        row.add(OFFLINE_SESSION)
        row1.add(OFFLINE)

        keyboard.add(row)
        keyboard.add(row1)
        keyboardMarkup.keyboard = keyboard
        keyboardMarkup.resizeKeyboard = true
        sendMessage.replyMarkup = keyboardMarkup

        val dtoList = messageService.getAllMessagesNotRepliedByLanguage(message.from.id)
        if (dtoList.isNotEmpty()) {
            registerUser.state = OperatorState.BUSY.name
            userRepository.save(registerUser)
        }

        for (dto in dtoList) {
            sendMessage.text = dto.body
            sender.execute(sendMessage)
        }
    }

    private fun sendAnswer(message: Message, sender: AbsSender) {
//        val sendMessage = SendMessage()
//        val dto = OperatorMessageDto(message.text, message.from.id, null)
////        val sessions = messageService.operatorWriteMsg(dto)
//////        val question = messageRepository.findByTelegramMessageId(message.messageId)
//
//        sendMessage.chatId = sessions.user.chatId.toString()
//        sendMessage.text = message.text
//        sender.execute(sendMessage)

    }

    private fun back(message: Message, sender: AbsSender) {
        val registerUser = registerUser(message.from)
        return when (registerUser.state) {
            SHARE_CONTACT -> start(message, sender)
            QUESTION -> chooseLanguage(message, sender)
            else -> start(message, sender)
        }
    }

    override fun handle(message: Message, sender: AbsSender) {
        val telegramUser = message.from
        val chatId = telegramUser.id.toString()

        val sendMessage = SendMessage()   // chatId  text
        sendMessage.enableHtml(true)
        sendMessage.chatId = chatId


        if (message.hasText()) {
            when (message.text) {
                START -> start(message, sender)

                UZBEK_, RUSSIAN_, ENGLISH_ -> chooseLanguage(message, sender)

                BACK_UZ, BACK_RU, BACK_EN -> back(message, sender)

                SEND_QUESTION_EN, SEND_QUESTION_RU, SEND_QUESTION_UZ -> sendQuestion(message, sender)

                ONLINE_UZ, ONLINE_RU -> getQuestions(message, sender)

                else -> {
                    val registerUser = registerUser(message.from)
                    if (registerUser.state == SEND_QUESTION) {
                        handleQuestion(message, sender)
                    } else if (registerUser.state == OperatorState.BUSY.name) {
                        sendAnswer(message, sender)
                    } else {
                        val sendMessage = SendMessage()
                        sendMessage.chatId = registerUser.chatId.toString()
                        sendMessage.text = "botni qayta ishga tushirish uchun /start tugmasini bosing"
                        sender.execute(sendMessage)
                    }
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
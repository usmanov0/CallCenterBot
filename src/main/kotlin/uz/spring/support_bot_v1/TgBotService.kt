package uz.spring.support_bot_v1

import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.getForObject
import org.telegram.telegrambots.meta.api.methods.GetFile
import org.telegram.telegrambots.meta.api.methods.send.SendAnimation
import org.telegram.telegrambots.meta.api.methods.send.SendAudio
import org.telegram.telegrambots.meta.api.methods.send.SendDocument
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto
import org.telegram.telegrambots.meta.api.methods.send.SendSticker
import org.telegram.telegrambots.meta.api.methods.send.SendVideo
import org.telegram.telegrambots.meta.api.methods.send.SendVideoNote
import org.telegram.telegrambots.meta.api.methods.send.SendVoice
import org.telegram.telegrambots.meta.api.objects.CallbackQuery
import org.telegram.telegrambots.meta.api.objects.InputFile
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.User
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow
import org.telegram.telegrambots.meta.bots.AbsSender
import java.io.File

interface MessageHandler {
    fun handle(message: Message, sender: AbsSender)
}

interface CallbackQueryHandler {   // baholash
    fun handle(callbackQuery: CallbackQuery, sender: AbsSender)
}

@Service
class MessageHandlerImpl(
    private val userRepository: UserRepository,
    private val userService: UserService,
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
                    false,
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

        val sendMessage = SendMessage()
        sendMessage.chatId = message.chatId.toString()
        val firstName = message.chat.firstName
        val registerUser = registerUser(message.from)

        if (registerUser.state == BEFORE_SEND_QUESTION) {
            when (registerUser.language) {
                LanguageEnum.Uzbek -> sendMessage.text = SETTINGS_UZ
                LanguageEnum.English -> sendMessage.text = SETTINGS_EN
                LanguageEnum.Russian -> sendMessage.text = SETTINGS_RU
                null -> sendMessage.text = SETTINGS_EN
            }
        } else
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
        registerUser.language = language
        userRepository.save(registerUser)

        if (registerUser.state == BEFORE_SEND_QUESTION) {
            getContact(message, sender)
        } else {
            registerUser.state = SHARE_CONTACT
            userRepository.save(registerUser)

            if (registerUser.phone != null)
                getContact(message, sender)
            else {

                val language1 = registerUser.language!!  //   [Russian]

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

    }

    private fun sendQuestion(message: Message, sender: AbsSender) {
        val sendMessage = SendMessage()
        sendMessage.chatId = message.chatId.toString()

        val registerUser = registerUser(message.from)

        registerUser.state = SEND_QUESTION

        userRepository.save(registerUser)

        val language1 = registerUser.language!!  //   [Russian]

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

    private fun secondQuestion(message: Message, sender: AbsSender) {

        val registerUser = registerUser(message.from)
        val language1 = registerUser.language!!  //   [Russian]

        if (message.hasText()) {
            val sendMessage = SendMessage()
            when (language1.toString()) {
                UZBEK -> {
                    val userWriteMsg = messageService.userWriteMsg(
                        UserMessageDto(
                            message.text,
                            registerUser.chatId,
                            registerUser.language!!.name
                        )
                    )
                    if (userWriteMsg != null) {
                        val operatorChatId = userWriteMsg.operatorChatId
                        sendMessage.chatId = operatorChatId.toString()
                        sendMessage.text = message.text
                        sender.execute(sendMessage)
                    }
                }

                RUSSIAN -> {
                    val userWriteMsg = messageService.userWriteMsg(
                        UserMessageDto(
                            message.text,
                            registerUser.chatId,
                            registerUser.language!!.name
                        )
                    )
                    if (userWriteMsg != null) {
                        val operatorChatId = userWriteMsg.operatorChatId
                        sendMessage.chatId = operatorChatId.toString()
                        sendMessage.text = message.text
                        sender.execute(sendMessage)
                    }
                }

                ENGLISH -> {
                    val userWriteMsg = messageService.userWriteMsg(
                        UserMessageDto(
                            message.text,
                            registerUser.chatId,
                            registerUser.language!!.name
                        )
                    )
                    if (userWriteMsg != null) {
                        val operatorChatId = userWriteMsg.operatorChatId
                        sendMessage.chatId = operatorChatId.toString()
                        sendMessage.text = message.text
                        sender.execute(sendMessage)
                    }
                }

            }
        }
        if (message.hasAnimation()) {
            val animation = message.animation

            val content = getFromTelegram(animation.fileId, sender)

            val fileDto = messageService.userWriteFile(
                UserFileDto(
                    "${animation.fileUniqueId}.gif",
                    null,
                    "ANIMATION",
                    registerUser.chatId,
                    registerUser.language!!.name,
                    content
                )
            )
            if (fileDto != null) {
                val sendAnimation = SendAnimation(
                    fileDto.operatorChatId.toString(),
                    InputFile(File(basePath + "\\" + fileDto.fileName))
                )
                sender.execute(sendAnimation)
            }


        }
        else if (message.hasAudio()) {
            val audio = message.audio

            val content = getFromTelegram(audio.fileId, sender)

            val fileDto = messageService.userWriteFile(
                UserFileDto(
                    audio.fileUniqueId,
                    null,
                    "MUSIC",
                    registerUser.chatId,
                    registerUser.language!!.name,
                    content
                )
            )
            if (fileDto != null) {
                val sendAudio = SendAudio(
                    fileDto.operatorChatId.toString(),
                    InputFile(File(basePath + "\\" + fileDto.fileName))
                )
                sender.execute(sendAudio)
            }


        }
        else if (message.hasDocument()) {
            val document = message.document

            val content = getFromTelegram(document.fileId, sender)

            val fileDto = messageService.userWriteFile(
                UserFileDto(
                    document.fileUniqueId,
                    null,
                    "DOCUMENT",
                    registerUser.chatId,
                    registerUser.language!!.name,
                    content
                )
            )
            if (fileDto != null) {
                val sendDocument = SendDocument(
                    fileDto.operatorChatId.toString(),
                    InputFile(File(basePath + "\\" + fileDto.fileName))
                )
                sender.execute(sendDocument)
            }


        }
        else if (message.hasPhoto()) {
            val photo = message.photo

            val content = getFromTelegram(photo[1].fileId, sender)

            val fileDto = messageService.userWriteFile(
                UserFileDto(
                    "${photo[1].fileUniqueId}.png",
                    null,
                    "PHOTO",
                    registerUser.chatId,
                    registerUser.language!!.name,
                    content
                )
            )
            if (fileDto != null) {
                val sendPhoto = SendPhoto(
                    fileDto.operatorChatId.toString(),
                    InputFile(File(basePath + "\\" + fileDto.fileName))
                )
                sender.execute(sendPhoto)
            }

        }
        else if (message.hasVideo()) {
            val video = message.video

            val content = getFromTelegram(video.fileId, sender)

            val fileDto = messageService.userWriteFile(
                UserFileDto(
                    "${video.fileUniqueId}.mp4",
                    null,
                    "VIDEO",
                    registerUser.chatId,
                    registerUser.language!!.name,
                    content
                )
            )
            if (fileDto != null) {
                val sendVideo = SendVideo(
                    fileDto.operatorChatId.toString(),
                    InputFile(File(basePath + "\\" + fileDto.fileName))
                )
                sender.execute(sendVideo)
            }

        }
        else if (message.hasVideoNote()) {
            val videoNote = message.videoNote

            val content = getFromTelegram(videoNote.fileId, sender)

            val fileDto = messageService.userWriteFile(
                UserFileDto(
                    videoNote.fileUniqueId,
                    null,
                    "VIDEO_NOTE",
                    registerUser.chatId,
                    registerUser.language!!.name,
                    content
                )
            )
            if (fileDto != null) {
                val sendVideoNote = SendVideoNote(
                    fileDto.operatorChatId.toString(),
                    InputFile(File(basePath + "\\" + fileDto.fileName))
                )
                sender.execute(sendVideoNote)
            }

        }
        else if (message.hasSticker()) {
            val sticker = message.sticker

            val content = getFromTelegram(sticker.fileId, sender)

            val fileDto = messageService.userWriteFile(
                UserFileDto(
                    sticker.fileUniqueId,
                    null,
                    "STICKER",
                    registerUser.chatId,
                    registerUser.language!!.name,
                    content
                )
            )
            if (fileDto != null) {
                val sendSticker = SendSticker(
                    fileDto.operatorChatId.toString(),
                    InputFile(File(basePath + "\\" + fileDto.fileName))
                )
                sender.execute(sendSticker)
            }

        }
        else if (message.hasVoice()) {
            val voice = message.voice

            val content = getFromTelegram(voice.fileId, sender)

            val fileDto = messageService.userWriteFile(
                UserFileDto(
                    "${voice.fileUniqueId}.ogg",
                    null,
                    "VOICE",
                    registerUser.chatId,
                    registerUser.language!!.name,
                    content
                )
            )
            if (fileDto != null) {
                val sendVoice = SendVoice(
                    fileDto.operatorChatId.toString(),
                    InputFile(File(basePath + "\\" + fileDto.fileName))
                )
                sender.execute(sendVoice)
            }

        }


    }

    private fun getQuestions(message: Message, sender: AbsSender) {
        var temp: Boolean = false
        val sendMessage = SendMessage()
        val registerUser = registerUser(message.from)
        registerUser.state = SEND_ANSWER
        userRepository.save(registerUser)

        val operatorChatId = message.from.id
        sendMessage.chatId = operatorChatId.toString()

        val list = userService.onlineOperator(operatorChatId)

        if (list != null) {
            temp = true
            for (i in 0..list.size - 2) {
                sendMessage.text = list[i].body!!
                sender.execute(sendMessage)
            }
            sendMessage.text = list[list.size - 1].body!!
        }
        if (!temp)
            sendMessage.text = "Sizda hozircha habar yoq"

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
        sender.execute(sendMessage)
    }

    private fun closeChat(message: Message, sender: AbsSender) {
        var temp: Boolean = false
        val sendMessage = SendMessage()
        val operatorChatId = message.from.id
        sendMessage.chatId = operatorChatId.toString()
        sessionRepository.findByOperatorChatIdAndActiveTrue(operatorChatId)?.let {
            val chatId = it.user.chatId
            val sendMessage1 = SendMessage()
            sendMessage1.chatId = chatId.toString()
            val user = it.user
            user.state = BEFORE_SEND_QUESTION
            userRepository.save(user)

            when (it.chatLanguage) {
                LanguageEnum.Uzbek -> {
                    sendMessage1.text = "Suhbat yakunlandi !"

                    val keyboardMarkup = ReplyKeyboardMarkup()
                    val keyboard: MutableList<KeyboardRow> = ArrayList()
                    val row = KeyboardRow()
                    val row1 = KeyboardRow()

                    row.add(SEND_QUESTION_UZ)
                    row.add(SETTING_UZ)

                    keyboard.add(row)
                    keyboard.add(row1)
                    keyboardMarkup.keyboard = keyboard
                    keyboardMarkup.resizeKeyboard = true
                    sendMessage1.replyMarkup = keyboardMarkup

                    sender.execute(sendMessage1)

                }

                LanguageEnum.English -> {
                    sendMessage1.text = "Suhbat yakunlandi !"

                    val keyboardMarkup = ReplyKeyboardMarkup()
                    val keyboard: MutableList<KeyboardRow> = ArrayList()
                    val row = KeyboardRow()
                    val row1 = KeyboardRow()

                    row.add(SEND_QUESTION_EN)
                    row.add(SETTING_EN)

                    keyboard.add(row)
                    keyboard.add(row1)
                    keyboardMarkup.keyboard = keyboard
                    keyboardMarkup.resizeKeyboard = true
                    sendMessage1.replyMarkup = keyboardMarkup

                    sender.execute(sendMessage1)
                }

                LanguageEnum.Russian -> {
                    sendMessage1.text = "Suhbat yakunlandi !"

                    val keyboardMarkup = ReplyKeyboardMarkup()
                    val keyboard: MutableList<KeyboardRow> = ArrayList()
                    val row = KeyboardRow()
                    val row1 = KeyboardRow()

                    row.add(SEND_QUESTION_RU)
                    row.add(SETTING_RU)

                    keyboard.add(row)
                    keyboard.add(row1)
                    keyboardMarkup.keyboard = keyboard
                    keyboardMarkup.resizeKeyboard = true
                    sendMessage1.replyMarkup = keyboardMarkup

                    sender.execute(sendMessage1)
                }
            }
        }

        val list = userService.closeSession(operatorChatId)

        if (list != null) {
            temp = true
            for (i in 0..list.size - 2) {
                sendMessage.text = list[i].body!!
                sender.execute(sendMessage)
            }
            sendMessage.text = list[list.size - 1].body!!
        }
        if (!temp)
            sendMessage.text = "Hozircha habar yo'q"
        val keyboardMarkup = ReplyKeyboardMarkup()
        val keyboard: MutableList<KeyboardRow> = ArrayList()
        val row = KeyboardRow()

        row.add(OFFLINE_SESSION)
        row.add(OFFLINE)

        keyboard.add(row)
        keyboardMarkup.keyboard = keyboard
        keyboardMarkup.resizeKeyboard = true
        sendMessage.replyMarkup = keyboardMarkup
        sender.execute(sendMessage)
    }

    private fun sendAnswer(message: Message, sender: AbsSender) {
        val sendMessage = SendMessage()

        val dto = messageService.operatorWriteMsg(
            OperatorMessageDto(
                message.text,
                message.from.id,
                null
            )
        )
        sendMessage.chatId = dto.userChatId.toString()
        sendMessage.text = message.text
        sender.execute(sendMessage)

    }

    private fun offline(message: Message, sender: AbsSender) {
        val sendMessage = SendMessage()
        val operatorChatId = message.from.id
        sendMessage.chatId = operatorChatId.toString()
        userService.offlineOperator(operatorChatId)
        sendMessage.text = "Ish yakunlandi !"

        val keyboardMarkup = ReplyKeyboardMarkup()
        val keyboard: MutableList<KeyboardRow> = ArrayList()
        val row1 = KeyboardRow()

        row1.add(ONLINE_UZ)

        keyboard.add(row1)
        keyboardMarkup.keyboard = keyboard
        keyboardMarkup.resizeKeyboard = true
        sendMessage.replyMarkup = keyboardMarkup
        sender.execute(sendMessage)
    }

    private fun getContact(message: Message, sender: AbsSender) {
        val registerUser = registerUser(message.from)
        val sendMessage = SendMessage()
        if (registerUser.state == SHARE_CONTACT) {
            val phone: String = if (message.hasContact())
                message.contact.phoneNumber
            else {
                message.text
            }
            val language1 = registerUser.language!!
            sendMessage.chatId = message.from.id.toString()

            val regex = Regex("^(\\+998|998)\\d{9}$")
            val matches = regex.matches(phone.trim(' '))
            if (matches) {

                registerUser.state = BEFORE_SEND_QUESTION
                registerUser.phone = phone
                userRepository.save(registerUser)

                temp(sendMessage, language1)

                sender.execute(sendMessage)

            } else {
                when (registerUser.language) {
                    LanguageEnum.Uzbek -> sendMessage.text = "Telefoninggizni ushbu ko'rinishda kiriting 998901234567"
                    LanguageEnum.English -> sendMessage.text = "Enter your phone in this view 998901234567"
                    LanguageEnum.Russian -> sendMessage.text = "Введите свой телефон в этом представлении 998901234567"
                    else -> {}
                }
                sender.execute(sendMessage)
            }
        } else if (registerUser.state == BEFORE_SEND_QUESTION) {
            sendMessage.chatId = registerUser.chatId.toString()
            temp(sendMessage, registerUser.language!!)
            sender.execute(sendMessage)
        }
    }

    private fun temp(sendMessage: SendMessage, language1: LanguageEnum) {
        when (language1.toString()) {
            UZBEK -> {
                sendMessage.text = CHOOSE_OPTION_UZ
                val keyboardMarkup = ReplyKeyboardMarkup()
                val keyboard: MutableList<KeyboardRow> = ArrayList()
                val row = KeyboardRow()
                val row1 = KeyboardRow()

                row.add(SEND_QUESTION_UZ)
                row.add(SETTING_UZ)

                keyboard.add(row)
                keyboard.add(row1)
                keyboardMarkup.keyboard = keyboard
                keyboardMarkup.resizeKeyboard = true
                sendMessage.replyMarkup = keyboardMarkup
            }

            RUSSIAN -> {
                sendMessage.text = CHOOSE_OPTION_RU
                val keyboardMarkup = ReplyKeyboardMarkup()
                val keyboard: MutableList<KeyboardRow> = ArrayList()
                val row = KeyboardRow()
                val row1 = KeyboardRow()

                row.add(SEND_QUESTION_RU)
                row.add(SETTING_RU)

                keyboard.add(row)
                keyboard.add(row1)
                keyboardMarkup.keyboard = keyboard
                keyboardMarkup.resizeKeyboard = true
                sendMessage.replyMarkup = keyboardMarkup
            }

            ENGLISH -> {
                sendMessage.text = CHOOSE_OPTION_EN
                val keyboardMarkup = ReplyKeyboardMarkup()
                val keyboard: MutableList<KeyboardRow> = ArrayList()
                val row = KeyboardRow()
                val row1 = KeyboardRow()

                row.add(SEND_QUESTION_EN)
                row.add(SETTING_EN)

                keyboard.add(row)
                keyboard.add(row1)
                keyboardMarkup.keyboard = keyboard
                keyboardMarkup.resizeKeyboard = true
                sendMessage.replyMarkup = keyboardMarkup
            }
        }
    }

    private fun back(message: Message, sender: AbsSender) {
        val registerUser = registerUser(message.from)
        return when (registerUser.state) {
            SHARE_CONTACT -> start(message, sender)
            BEFORE_SEND_QUESTION -> {
                registerUser.state = CHOOSE_LANGUAGE
                userRepository.save(registerUser)
                start(message, sender)
            }

            else -> start(message, sender)
        }
    }

//    private fun testVideoGolosFile(message: Message, sender: AbsSender) {
//        if (message.hasAnimation()) {
////            val sendAnimation:SendAnimation()
////            sendAnimation
//        }else if (message.hasAudio()){
////            val sendMessage:SendMessage()
//
//        }else if (message.hasDocument()){
//
//        }else if (message.hasPhoto()){
//
//        }else if (message.hasVideo()){
//
//        }else if (message.hasVideoNote()){
//
//        }else if (message.hasVoice()){
//
//        }
//    }

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

                SETTING_UZ, SETTING_RU, SETTING_EN -> start(message, sender)

                SEND_QUESTION_UZ, SEND_QUESTION_RU, SEND_QUESTION_EN -> sendQuestion(
                    message,
                    sender
                )

                ONLINE_UZ, ONLINE_RU -> getQuestions(message, sender)

                OFFLINE_SESSION -> closeChat(message, sender)

                OFFLINE -> offline(message, sender)

//                "/video" -> testVideoGolosFile(message, sender)

                "/reply" -> {
                    sendMessage.chatId = "341330802"
                    sendMessage.text = "salom"
                    sendMessage.replyToMessageId = 5764
                    sender.execute(sendMessage)
                }

                else -> {
                    val registerUser = registerUser(message.from)
                    when (registerUser.state) {
                        SEND_QUESTION -> {
                            secondQuestion(message, sender)
                        }

                        SEND_ANSWER -> {
                            sendAnswer(message, sender)
                        }

                        SHARE_CONTACT -> getContact(message, sender)

                        else -> {
                            sendMessage.chatId = registerUser.chatId.toString()
                            sendMessage.text = "botni qayta ishga tushirish uchun /start tugmasini bosing"
                            sender.execute(sendMessage)
                        }
                    }
                }
            }

        } else if (message.hasContact()) {
            val registerUser = registerUser(message.from)

            if (registerUser.language == null) {
                sendMessage.chatId = registerUser.chatId.toString()
                sendMessage.text = "botni qayta ishga tushirish uchun /start tugmasini bosing"
                sender.execute(sendMessage)
            } else {
                getContact(message, sender)
            }
        } else if ((message.hasVoice() || message.hasVideoNote() || message.hasVideo() || message.hasPhoto() || message.hasDocument() || message.hasAudio() || message.hasAnimation() || message.hasSticker()) && registerUser(
                message.from
            ).state == SEND_QUESTION
        ) {
            secondQuestion(message, sender)
        }
    }

    fun getFromTelegram(fileId: String, sender: AbsSender) = sender.execute(GetFile(fileId)).run {
        RestTemplate().getForObject<ByteArray>("https://api.telegram.org/file/bot${token1}/${filePath}")
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

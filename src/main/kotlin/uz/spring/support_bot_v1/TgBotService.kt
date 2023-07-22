package uz.spring.support_bot_v1

import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.getForObject
import org.telegram.telegrambots.meta.api.methods.GetFile
import org.telegram.telegrambots.meta.api.methods.send.SendAnimation
import org.telegram.telegrambots.meta.api.methods.send.SendAudio
import org.telegram.telegrambots.meta.api.methods.send.SendContact
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
                    STATE_START,
                    tgUser.lastName,
                    tgUser.firstName
                )
            )
    }


    private fun startUser(message: Message, sender: AbsSender) {

        val sendMessage = SendMessage()
        sendMessage.chatId = message.chatId.toString()
        val firstName = message.chat.firstName
        val registerUser = registerUser(message.from)
        registerUser.state = CHOOSE_LANGUAGE
        userRepository.save(registerUser)
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

    private fun changeLanguage(message: Message, sender: AbsSender) {
        val sendMessage = SendMessage()
        sendMessage.chatId = message.chatId.toString()
        val registerUser = registerUser(message.from)
        registerUser.state = CHANGE_LANGUAGE
        userRepository.save(registerUser)

        when (registerUser.language) {
            LanguageEnum.Uzbek -> sendMessage.text = SETTINGS_UZ
            LanguageEnum.English -> sendMessage.text = SETTINGS_EN
            LanguageEnum.Russian -> sendMessage.text = SETTINGS_RU
            null -> sendMessage.text = SETTINGS_EN
        }

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
        val registerUser = registerUser(message.from)
        registerUser.state = AFTER_START_OPERATOR
        userRepository.save(registerUser)
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
        else if (message.hasAnimation()) {
            val animation = message.animation

            val content = getFromTelegram(animation.fileId, sender)
            val list = animation.mimetype.split("/")
            val type = list[list.size - 1]


            val fileDto = messageService.userWriteFile(
                UserFileDto(
                    "${animation.fileUniqueId}.${type}",
                    null,
                    "ANIMATION",
                    registerUser.chatId,
                    registerUser.language!!.name,
                    content
                )
            )
            if (fileDto != null) {
                val sendAnimation = SendAnimation(
                    fileDto.chatId.toString(),
                    InputFile(File(basePath + "\\" + fileDto.fileName))
                )
                sender.execute(sendAnimation)
            }

        }
        else if (message.hasContact()) {
            val contact = message.contact
            val sendContact = SendContact()
            val userWriteMsg = messageService.userWriteMsg(
                UserMessageDto(
                    contact.phoneNumber,
                    registerUser.chatId,
                    registerUser.language!!.name
                )
            )
            if (userWriteMsg != null) {
                val operatorChatId = userWriteMsg.operatorChatId
                sendContact.chatId = operatorChatId.toString()
                sendContact.firstName = registerUser.firstName
                sendContact.phoneNumber = contact.phoneNumber
                sender.execute(sendContact)
            }


        }
        else if (message.hasAudio()) {
            val audio = message.audio

            val content = getFromTelegram(audio.fileId, sender)
            val list = audio.mimeType.split("/")
            val type = list[list.size - 1]

            val fileDto = messageService.userWriteFile(
                UserFileDto(
                    "${audio.fileUniqueId}.${type}",
                    null,
                    "AUDIO",
                    registerUser.chatId,
                    registerUser.language!!.name,
                    content
                )
            )
            if (fileDto != null) {
                val sendAudio = SendAudio(
                    fileDto.chatId.toString(),
                    InputFile(File(basePath + "\\" + fileDto.fileName))
                )
                sender.execute(sendAudio)
            }


        }
        else if (message.hasDocument()) {
            val document = message.document

            val content = getFromTelegram(document.fileId, sender)
            val list = document.mimeType.split("/")
            val type = list[list.size - 1]

            val fileDto = messageService.userWriteFile(
                UserFileDto(
                    "${document.fileName}--${document.fileUniqueId}.${type}",
                    null,
                    "DOCUMENT",
                    registerUser.chatId,
                    registerUser.language!!.name,
                    content
                )
            )
            if (fileDto != null) {
                val sendDocument = SendDocument(
                    fileDto.chatId.toString(),
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
                    fileDto.chatId.toString(),
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
                    fileDto.chatId.toString(),
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
                    fileDto.chatId.toString(),
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
                    fileDto.chatId.toString(),
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
                    fileDto.chatId.toString(),
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
                val item = list[i].fileDto
                if (item != null) {
                    when (item.contentType) {
                        "VIDEO" -> {
                            val sendVideo = SendVideo(
                                operatorChatId.toString(),
                                InputFile(File(basePath + "\\" + item.fileName))
                            )

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
                            sendVideo.replyMarkup = keyboardMarkup
                            sender.execute(sendVideo)

                        }

                        "DOCUMENT" -> {
                            val sendDocument = SendDocument(
                                operatorChatId.toString(),
                                InputFile(File(basePath + "\\" + item.fileName))
                            )

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
                            sendDocument.replyMarkup = keyboardMarkup

                            sender.execute(sendDocument)


                        }

                        "ANIMATION" -> {
                            val sendAnimation = SendAnimation(
                                operatorChatId.toString(),
                                InputFile(File(basePath + "\\" + item.fileName))
                            )

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
                            sendAnimation.replyMarkup = keyboardMarkup

                            sender.execute(sendAnimation)
                        }

                        "VOICE" -> {
                            val sendVoice = SendVoice(
                                operatorChatId.toString(),
                                InputFile(File(basePath + "\\" + item.fileName))
                            )

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
                            sendVoice.replyMarkup = keyboardMarkup

                            sender.execute(sendVoice)


                        }

                        "STICKER" -> {
                            val sendSticker = SendSticker(
                                operatorChatId.toString(),
                                InputFile(File(basePath + "\\" + item.fileName))
                            )

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
                            sendSticker.replyMarkup = keyboardMarkup

                            sender.execute(sendSticker)


                        }

                        "VIDEO_NOTE" -> {
                            val sendVideoNote = SendVideoNote(
                                operatorChatId.toString(),
                                InputFile(File(basePath + "\\" + item.fileName))
                            )

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
                            sendVideoNote.replyMarkup = keyboardMarkup
                            sender.execute(sendVideoNote)

                        }

                        "AUDIO" -> {
                            val sendAudio = SendAudio(
                                operatorChatId.toString(),
                                InputFile(File(basePath + "\\" + item.fileName))
                            )
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
                            sendAudio.replyMarkup = keyboardMarkup

                            sender.execute(sendAudio)


                        }

                        "PHOTO" -> {
                            val sendPhoto = SendPhoto(
                                operatorChatId.toString(),
                                InputFile(File(basePath + "\\" + item.fileName))
                            )

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
                            sendPhoto.replyMarkup = keyboardMarkup

                            sender.execute(sendPhoto)
                        }
                    }
                } else {
                    val keyboardMarkup = ReplyKeyboardMarkup()
                    val keyboard: MutableList<KeyboardRow> = ArrayList()
                    val row = KeyboardRow()
                    val row1 = KeyboardRow()
                    sendMessage.text = list[i].body!!

                    row.add(OFFLINE_SESSION)
                    row1.add(OFFLINE)
                    keyboard.add(row)
                    keyboard.add(row1)
                    keyboardMarkup.keyboard = keyboard
                    keyboardMarkup.resizeKeyboard = true
                    sendMessage.replyMarkup = keyboardMarkup
                    sender.execute(sendMessage)
                }
            }
            val item = list[list.size - 1].fileDto
            if (item != null) {
                when (item.contentType) {
                    "VIDEO" -> {
                        val sendVideo = SendVideo(
                            operatorChatId.toString(),
                            InputFile(File(basePath + "\\" + item.fileName))
                        )
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
                        sendVideo.replyMarkup = keyboardMarkup

                        sender.execute(sendVideo)

                    }

                    "DOCUMENT" -> {
                        val sendDocument = SendDocument(
                            operatorChatId.toString(),
                            InputFile(File(basePath + "\\" + item.fileName))
                        )

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
                        sendDocument.replyMarkup = keyboardMarkup

                        sender.execute(sendDocument)


                    }

                    "ANIMATION" -> {
                        val sendAnimation = SendAnimation(
                            operatorChatId.toString(),
                            InputFile(File(basePath + "\\" + item.fileName))
                        )

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
                        sendAnimation.replyMarkup = keyboardMarkup

                        sender.execute(sendAnimation)
                    }

                    "VOICE" -> {
                        val sendVoice = SendVoice(
                            operatorChatId.toString(),
                            InputFile(File(basePath + "\\" + item.fileName))
                        )

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
                        sendVoice.replyMarkup = keyboardMarkup

                        sender.execute(sendVoice)


                    }

                    "STICKER" -> {
                        val sendSticker = SendSticker(
                            operatorChatId.toString(),
                            InputFile(File(basePath + "\\" + item.fileName))
                        )

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
                        sendSticker.replyMarkup = keyboardMarkup

                        sender.execute(sendSticker)


                    }

                    "VIDEO_NOTE" -> {
                        val sendVideoNote = SendVideoNote(
                            operatorChatId.toString(),
                            InputFile(File(basePath + "\\" + item.fileName))
                        )

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
                        sendVideoNote.replyMarkup = keyboardMarkup

                        sender.execute(sendVideoNote)


                    }

                    "AUDIO" -> {
                        val sendAudio = SendAudio(
                            operatorChatId.toString(),
                            InputFile(File(basePath + "\\" + item.fileName))
                        )

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
                        sendAudio.replyMarkup = keyboardMarkup

                        sender.execute(sendAudio)


                    }

                    "PHOTO" -> {
                        val sendPhoto = SendPhoto(
                            operatorChatId.toString(),
                            InputFile(File(basePath + "\\" + item.fileName))
                        )

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
                        sendPhoto.replyMarkup = keyboardMarkup

                        sender.execute(sendPhoto)
                    }
                }
            } else {
                val keyboardMarkup = ReplyKeyboardMarkup()
                val keyboard: MutableList<KeyboardRow> = ArrayList()
                val row = KeyboardRow()
                val row1 = KeyboardRow()
                sendMessage.text = list[list.size - 1].body!!
                row.add(OFFLINE_SESSION)
                row1.add(OFFLINE)
                keyboard.add(row)
                keyboard.add(row1)
                keyboardMarkup.keyboard = keyboard
                keyboardMarkup.resizeKeyboard = true
                sendMessage.replyMarkup = keyboardMarkup
                sender.execute(sendMessage)
            }
        }
        if (!temp) {
            val keyboardMarkup = ReplyKeyboardMarkup()
            val keyboard: MutableList<KeyboardRow> = ArrayList()
            val row1 = KeyboardRow()
            sendMessage.text = "Sizda hozircha habar yoq"
            row1.add(OFFLINE)
            keyboard.add(row1)
            keyboardMarkup.keyboard = keyboard
            keyboardMarkup.resizeKeyboard = true
            sendMessage.replyMarkup = keyboardMarkup
            sender.execute(sendMessage)
        }
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
            user.state = RATE_OPERATOR
            userRepository.save(user)

            when (it.chatLanguage) {
                LanguageEnum.Uzbek -> {
                    sendMessage1.text = "Operatorni baholang !"

                    val keyboardMarkup = ReplyKeyboardMarkup()
                    val keyboard: MutableList<KeyboardRow> = ArrayList()
                    val row = KeyboardRow()

                    row.add(ONE)
                    row.add(TWO)
                    row.add(THREE)
                    row.add(FOUR)
                    row.add(FIVE)

                    keyboard.add(row)
                    keyboardMarkup.keyboard = keyboard
                    keyboardMarkup.resizeKeyboard = true
                    sendMessage1.replyMarkup = keyboardMarkup

                    sender.execute(sendMessage1)

                }

                LanguageEnum.English -> {
                    sendMessage1.text = "Rate the operator !"

                    val keyboardMarkup = ReplyKeyboardMarkup()
                    val keyboard: MutableList<KeyboardRow> = ArrayList()
                    val row = KeyboardRow()
                    val row1 = KeyboardRow()

                    row.add(ONE)
                    row.add(TWO)
                    row.add(THREE)
                    row.add(FOUR)
                    row.add(FIVE)

                    keyboard.add(row)
                    keyboard.add(row1)
                    keyboardMarkup.keyboard = keyboard
                    keyboardMarkup.resizeKeyboard = true
                    sendMessage1.replyMarkup = keyboardMarkup

                    sender.execute(sendMessage1)
                }

                LanguageEnum.Russian -> {
                    sendMessage1.text = "Оцените оператора !"

                    val keyboardMarkup = ReplyKeyboardMarkup()
                    val keyboard: MutableList<KeyboardRow> = ArrayList()
                    val row = KeyboardRow()
                    val row1 = KeyboardRow()

                    row.add(ONE)
                    row.add(TWO)
                    row.add(THREE)
                    row.add(FOUR)
                    row.add(FIVE)

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
        val keyboardMarkup = ReplyKeyboardMarkup()
        val keyboard: MutableList<KeyboardRow> = ArrayList()
        val row = KeyboardRow()
        if (!temp) {
            sendMessage.text = "Hozircha habar yo'q"
            row.add(OFFLINE)
        } else {
            row.add(OFFLINE_SESSION)
            row.add(OFFLINE)
        }


        keyboard.add(row)
        keyboardMarkup.keyboard = keyboard
        keyboardMarkup.resizeKeyboard = true
        sendMessage.replyMarkup = keyboardMarkup
        sender.execute(sendMessage)
    }

    private fun sendAnswer(message: Message, sender: AbsSender) {
        val registerUser = registerUser(message.from)

        if (message.hasText()) {
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

        if (message.hasContact()) {

            val sendContact = SendContact()
            val dto = messageService.operatorWriteMsg(
                OperatorMessageDto(
                    message.contact.phoneNumber,
                    message.from.id,
                    null
                )
            )
            sendContact.chatId = dto.userChatId.toString()
            sendContact.firstName = message.contact.firstName
            sendContact.phoneNumber = message.contact.phoneNumber
            sender.execute(sendContact)
        }

        if (message.hasAnimation()) {
            val animation = message.animation

            val content = getFromTelegram(animation.fileId, sender)

            val fileDto = messageService.operatorWriteFile(
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
                    fileDto.chatId.toString(),
                    InputFile(File(basePath + "\\" + fileDto.fileName))
                )
                sender.execute(sendAnimation)
            }


        } else if (message.hasAudio()) {
            val audio = message.audio

            val content = getFromTelegram(audio.fileId, sender)

            val fileDto = messageService.operatorWriteFile(
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
                    fileDto.chatId.toString(),
                    InputFile(File(basePath + "\\" + fileDto.fileName))
                )
                sender.execute(sendAudio)
            }


        } else if (message.hasDocument()) {
            val document = message.document

            val content = getFromTelegram(document.fileId, sender)

            val fileDto = messageService.operatorWriteFile(
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
                    fileDto.chatId.toString(),
                    InputFile(File(basePath + "\\" + fileDto.fileName))
                )
                sender.execute(sendDocument)
            }


        } else if (message.hasPhoto()) {
            val photo = message.photo

            val content = getFromTelegram(photo[1].fileId, sender)

            val fileDto = messageService.operatorWriteFile(
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
                    fileDto.chatId.toString(),
                    InputFile(File(basePath + "\\" + fileDto.fileName))
                )
                sender.execute(sendPhoto)
            }

        } else if (message.hasVideo()) {
            val video = message.video

            val content = getFromTelegram(video.fileId, sender)

            val fileDto = messageService.operatorWriteFile(
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
                    fileDto.chatId.toString(),
                    InputFile(File(basePath + "\\" + fileDto.fileName))
                )
                sender.execute(sendVideo)
            }

        } else if (message.hasVideoNote()) {
            val videoNote = message.videoNote

            val content = getFromTelegram(videoNote.fileId, sender)

            val fileDto = messageService.operatorWriteFile(
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
                    fileDto.chatId.toString(),
                    InputFile(File(basePath + "\\" + fileDto.fileName))
                )
                sender.execute(sendVideoNote)
            }

        } else if (message.hasSticker()) {
            val sticker = message.sticker

            val content = getFromTelegram(sticker.fileId, sender)

            val fileDto = messageService.operatorWriteFile(
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
                    fileDto.chatId.toString(),
                    InputFile(File(basePath + "\\" + fileDto.fileName))
                )
                sender.execute(sendSticker)
            }

        } else if (message.hasVoice()) {
            val voice = message.voice

            val content = getFromTelegram(voice.fileId, sender)

            val fileDto = messageService.operatorWriteFile(
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
                    fileDto.chatId.toString(),
                    InputFile(File(basePath + "\\" + fileDto.fileName))
                )
                sender.execute(sendVoice)
            }

        }


    }

    private fun offline(message: Message, sender: AbsSender) {
        val sendMessage = SendMessage()
        val operatorChatId = message.from.id
        sendMessage.chatId = operatorChatId.toString()


        sessionRepository.findByOperatorChatIdAndActiveTrue(operatorChatId)?.let {
            val chatId = it.user.chatId
            val sendMessage1 = SendMessage()
            sendMessage1.chatId = chatId.toString()
            val user = it.user
            user.state = RATE_OPERATOR
            userRepository.save(user)

            when (it.chatLanguage) {
                LanguageEnum.Uzbek -> {
                    sendMessage1.text = "Operatorni baholang !"

                    val keyboardMarkup = ReplyKeyboardMarkup()
                    val keyboard: MutableList<KeyboardRow> = ArrayList()
                    val row = KeyboardRow()

                    row.add(ONE)
                    row.add(TWO)
                    row.add(THREE)
                    row.add(FOUR)
                    row.add(FIVE)

                    keyboard.add(row)
                    keyboardMarkup.keyboard = keyboard
                    keyboardMarkup.resizeKeyboard = true
                    sendMessage1.replyMarkup = keyboardMarkup

                    sender.execute(sendMessage1)

                }

                LanguageEnum.English -> {
                    sendMessage1.text = "Rate the operator !"

                    val keyboardMarkup = ReplyKeyboardMarkup()
                    val keyboard: MutableList<KeyboardRow> = ArrayList()
                    val row = KeyboardRow()
                    val row1 = KeyboardRow()

                    row.add(ONE)
                    row.add(TWO)
                    row.add(THREE)
                    row.add(FOUR)
                    row.add(FIVE)

                    keyboard.add(row)
                    keyboard.add(row1)
                    keyboardMarkup.keyboard = keyboard
                    keyboardMarkup.resizeKeyboard = true
                    sendMessage1.replyMarkup = keyboardMarkup

                    sender.execute(sendMessage1)
                }

                LanguageEnum.Russian -> {
                    sendMessage1.text = "Оцените оператора !"

                    val keyboardMarkup = ReplyKeyboardMarkup()
                    val keyboard: MutableList<KeyboardRow> = ArrayList()
                    val row = KeyboardRow()
                    val row1 = KeyboardRow()

                    row.add(ONE)
                    row.add(TWO)
                    row.add(THREE)
                    row.add(FOUR)
                    row.add(FIVE)

                    keyboard.add(row)
                    keyboard.add(row1)
                    keyboardMarkup.keyboard = keyboard
                    keyboardMarkup.resizeKeyboard = true
                    sendMessage1.replyMarkup = keyboardMarkup

                    sender.execute(sendMessage1)
                }
            }
        }


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
            if (message.contact != null && message.contact.userId == registerUser.chatId) {

                registerUser.state = BEFORE_SEND_QUESTION
                registerUser.phone = message.contact.phoneNumber
                userRepository.save(registerUser)

                temp(sendMessage, registerUser.language!!)
                sendMessage.chatId = message.from.id.toString()

                sender.execute(sendMessage)

            } else if (message.contact != null && message.contact.userId != registerUser.chatId) {
                sendMessage.chatId = registerUser.chatId.toString()
                when (registerUser.language) {
                    LanguageEnum.Uzbek -> sendMessage.text =
                        "Iltimos o'zinggizni telefon raqaminggizni kiriting !"

                    LanguageEnum.English -> sendMessage.text = "Please enter your phone number!"
                    LanguageEnum.Russian -> sendMessage.text =
                        "Пожалуйста введите ваш номер телефона!"

                    else -> {}
                }
                sender.execute(sendMessage)
            }
            if (message.text != null) {
                val phone = message.text
                val language1 = registerUser.language!!
                sendMessage.chatId = message.from.id.toString()

                val regex = Regex("^(\\+998|998)\\d{9}$")
                val matches = regex.matches(phone)
                if (matches) {

                    registerUser.state = BEFORE_SEND_QUESTION
                    registerUser.phone = phone
                    userRepository.save(registerUser)

                    temp(sendMessage, language1)

                    sender.execute(sendMessage)

                } else {
                    when (registerUser.language) {
                        LanguageEnum.Uzbek -> sendMessage.text =
                            "Telefoninggizni ushbu ko'rinishda kiriting 998901234567"

                        LanguageEnum.English -> sendMessage.text = "Enter your phone in this view 998901234567"
                        LanguageEnum.Russian -> sendMessage.text =
                            "Введите свой телефон в этом представлении 998901234567"

                        else -> {}
                    }
                    sender.execute(sendMessage)
                }
            }


        }
        /*else if (registerUser.state == BEFORE_SEND_QUESTION) {
            sendMessage.chatId = registerUser.chatId.toString()
            temp(sendMessage, registerUser.language!!)
            sender.execute(sendMessage)
        }*/
    }

    private fun option(message: Message, sender: AbsSender) {
        val from = message.from

        val rate = message.text
        var rate1 : Short? = null
        if (rate == ONE || rate == TWO || rate == THREE || rate == FOUR || rate == FIVE) {
            rate1 = rate.toShort()
        }else {
            rate1 = -1
        }
        userService.rateOperator(
            MarkOperatorDto(
                from.id,
                rate1
            )
        )
        val registerUser = registerUser(from)

        val sendMessage1 = SendMessage()
        sendMessage1.chatId = registerUser.chatId.toString()

        registerUser.state = BEFORE_SEND_QUESTION
        userRepository.save(registerUser)

        when (registerUser.language) {
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
                sendMessage1.text = "The conversation is over !"

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
                sendMessage1.text = "Разговор окончен !"

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

            else -> {}
        }

    }

    private fun optionAfterChangedLanguage(message: Message, sender: AbsSender) {
        val from = message.from
        val registerUser = registerUser(from)

        val language = when (message.text) {
            UZBEK_ -> LanguageEnum.Uzbek
            RUSSIAN_ -> LanguageEnum.Russian
            ENGLISH_ -> LanguageEnum.English
            else -> registerUser.language
        }
        registerUser.language = language
        userRepository.save(registerUser)

        val sendMessage1 = SendMessage()
        sendMessage1.chatId = registerUser.chatId.toString()

        registerUser.state = BEFORE_SEND_QUESTION
        userRepository.save(registerUser)

        when (registerUser.language) {
            LanguageEnum.Uzbek -> {
                sendMessage1.text = CHOOSE_OPTION_UZ

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
                sendMessage1.text = CHOOSE_OPTION_EN

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
                sendMessage1.text = CHOOSE_OPTION_RU

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

            else -> {}
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
            SHARE_CONTACT -> startUser(message, sender)
//            BEFORE_SEND_QUESTION -> {
//                registerUser.state = CHOOSE_LANGUAGE
//                userRepository.save(registerUser)
//                start(message, sender)
//            }
            else -> startUser(message, sender)
        }
    }


    override fun handle(message: Message, sender: AbsSender) {
        val telegramUser = message.from
        val chatId = telegramUser.id.toString()

        val sendMessage = SendMessage()   // chatId  text
        sendMessage.enableHtml(true)
        sendMessage.chatId = chatId

        val registerUser = registerUser(message.from)
        if (message.hasText()) {
            val text = message.text

            if (text == START && registerUser.state == STATE_START) startUser(message, sender)
            else if (text == START && registerUser.state == STATE_OFFLINE) startOperator(message, sender)
            else if ((text == UZBEK_ || text == RUSSIAN_ || text == ENGLISH_) && registerUser.state == CHOOSE_LANGUAGE) chooseLanguage(
                message,
                sender
            )
            else if ((text == BACK_UZ || text == BACK_RU || text == BACK_EN)) back(message, sender)
            else if ((text == SETTING_UZ || text == SETTING_RU || text == SETTING_EN) && registerUser.state == BEFORE_SEND_QUESTION)
                changeLanguage(message, sender)
            else if ((text == SEND_QUESTION_UZ || text == SEND_QUESTION_RU || text == SEND_QUESTION_EN) && registerUser.state == BEFORE_SEND_QUESTION)
                sendQuestion(
                    message,
                    sender
                )
            else if ((text == ONLINE_UZ || text == ONLINE_RU) && (registerUser.state == AFTER_START_OPERATOR || registerUser.state == STATE_OFFLINE))
                getQuestions(message, sender)
            else if (text == OFFLINE_SESSION && registerUser.state == SEND_ANSWER) closeChat(message, sender)
            else if (text == OFFLINE && registerUser.state == SEND_ANSWER)
                offline(message, sender)

            else {
                when (registerUser.state) {
                    SEND_QUESTION -> {
                        secondQuestion(message, sender)
                    }

                    SEND_ANSWER -> {
                        sendAnswer(message, sender)
                    }

                    CHANGE_LANGUAGE -> {
                        optionAfterChangedLanguage(message, sender)
                    }


                    SHARE_CONTACT -> getContact(message, sender)

                    RATE_OPERATOR -> option(message, sender)


                    else -> {

                        sendMessage.chatId = registerUser.chatId.toString()

                        when (registerUser.language) {
                            LanguageEnum.Uzbek -> sendMessage.text = INVALID_COMMAND_UZ
                            LanguageEnum.English -> sendMessage.text = INVALID_COMMAND_EN
                            LanguageEnum.Russian -> sendMessage.text = INVALID_COMMAND_RU
                            null -> sendMessage.text = INVALID_COMMAND_EN
                        }
                        sender.execute(sendMessage)

                    }
                }
            }

        } else if (registerUser.state == SHARE_CONTACT && message.hasContact()) {

            if (registerUser.language == null) {
                sendMessage.chatId = registerUser.chatId.toString()
                sendMessage.text = "botni qayta ishga tushirish uchun /start tugmasini bosing"
                sender.execute(sendMessage)
            } else {
                getContact(message, sender)
            }
        } else if ((message.hasContact() || message.hasVoice() || message.hasVideoNote() || message.hasVideo() || message.hasPhoto() || message.hasDocument() || message.hasAudio() || message.hasAnimation() || message.hasSticker()) && registerUser(
                message.from
            ).state == SEND_QUESTION
        ) {
            secondQuestion(message, sender)
        } else if ((message.hasContact() || message.hasVoice() || message.hasVideoNote() || message.hasVideo() || message.hasPhoto() || message.hasDocument() || message.hasAudio() || message.hasAnimation() || message.hasSticker()) && registerUser(
                message.from
            ).state == SEND_ANSWER
        ) {
            sendAnswer(message, sender)
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

package uz.spring.support_bot_v1

import jakarta.transaction.Transactional
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.Date


interface UserService {
    fun findById(id: Long): UserDto
    fun getAll(pageable: Pageable): Page<UserDto>

    fun addOperator(chatId: Long)

    fun getOperatorsByChatId(chatId: Long): GetOneOperatorDto

    fun getOperators(): List<GetOneOperatorDto>

    fun deleteOperator(chatId: Long)

    fun onlineOperator(operatorChatId: Long): List<MessageReplyDto>?

    fun closeSession(operatorChatId: Long): List<MessageReplyDto>?

    fun offlineOperator(operatorChatId: Long)

    fun rateOperator(dto: MarkOperatorDto)
}

interface MessageService {
    fun userWriteMsg(dto: UserMessageDto): OperatorMessageDto?
    fun userWriteFile(dto: UserFileDto): OperatorFileDto?
    fun operatorWriteMsg(dto: OperatorMessageDto): UserMessageDto
    fun operatorWriteFile(dot: UserFileDto): OperatorFileDto?

    fun getAllMessagesNotRepliedByLanguage(operatorId: Long): List<QuestionsForOperatorDto>

}

interface FileService {
    fun writeFile(fileCreateDto: FileCreateDto, messages: Messages)

    fun readFile(fileName: String) : ByteArray
}


interface LanguageService {
    fun createLanguage(name: String)

    fun updateLanguage(id: Long, dto: LanguageDto)

    fun getOneLanguage(id: Long): LanguageDto

    fun getAll(pageable: Pageable): Page<GetOneLanguageDto>

    fun delete(id: Long)

}


interface OperatorLanguageService {
    fun create(dto: OperatorLanguageDto)
}

@Service
class UserServiceImpl(
    private val userRepository: UserRepository,
    private val sessionRepository: SessionRepository,
    private val messageRepository: MessageRepository,
    private val operatorsLanguagesRepository: OperatorsLanguagesRepository,
    private val fileService: FileService,
    private val fileRepository: FileRepository
) : UserService {

    override fun findById(id: Long) = userRepository.findByChatIdAndDeletedFalse(id)?.let { UserDto.toDto(it) }
        ?: throw UserNotFoundException(id)

    override fun getAll(pageable: Pageable): Page<UserDto> =
        userRepository.findAllNotDeleted(pageable).map { UserDto.toDto(it) }

    override fun addOperator(chatId: Long) {
        val user = userRepository.findByChatIdAndDeletedFalse(chatId) ?: throw OperatorNotFoundException(chatId)
        user.role = Role.OPERATOR
        user.state = STATE_OFFLINE
        user.operatorState = OperatorState.NOT_BUSY
        userRepository.save(user)
    }

    override fun getOperators(): List<GetOneOperatorDto> =
        userRepository.findAllByRoleAndDeletedFalse(Role.OPERATOR).map { GetOneOperatorDto.toDo(it) }

    override fun getOperatorsByChatId(chatId: Long): GetOneOperatorDto {
        val operator = userRepository.findByChatIdAndDeletedFalse(chatId) ?: throw OperatorNotFoundException(chatId)
        return GetOneOperatorDto.toDo(operator)
    }

    override fun deleteOperator(chatId: Long) {
        val operator = userRepository.findByChatIdAndDeletedFalse(chatId) ?: throw OperatorNotFoundException(chatId)
        operator.role = Role.USER
        operator.operatorState = null
        userRepository.save(operator)
    }

    override fun onlineOperator(operatorChatId: Long): List<MessageReplyDto>? {
        val operator = userRepository.findByChatIdAndDeletedFalse(operatorChatId) ?: throw OperatorNotFoundException(
            operatorChatId
        )
        operator.isOnline = true
        operator.operatorState = OperatorState.NOT_BUSY
        operator.state = SEND_ANSWER
        userRepository.save(operator)
        val languages = operatorsLanguagesRepository.getAllLanguagesByOperatorId(operator.id!!)
        var sessions: Sessions? = null

        for (item in languages) {  // Uzbek  Russian
            val sessions1 = sessionRepository.getSession(item.name)
            if (sessions1.isNotEmpty()) {
                sessions = sessions1[0]
                break
            }
        }
        if (sessions != null && sessions.user.chatId != operatorChatId) {
            sessions.operator = operator
            sessionRepository.save(sessions)
            operator.operatorState = OperatorState.BUSY
            userRepository.save(operator)
            val messageList =
                messageRepository.findBySessionIdOrderByCreatedDate(sessions.id)
            val messageResponseDtoList = mutableListOf<MessageReplyDto>()
            for (message in messageList) {
                if (message.fileType == FileType.FILE) {
                    val fileEntity = fileRepository.findByMessagesId(message.id!!)
                    val content = fileService.readFile(fileEntity.fileName)
                    messageResponseDtoList.add(MessageReplyDto(message.body, FileResponseDto(fileEntity.fileName, basePath, fileEntity.contentType, content)))
                }else {
                    messageResponseDtoList.add(MessageReplyDto(message.body, null))
                }
            }
            return messageResponseDtoList
        }
        return null
    }

    override fun closeSession(operatorChatId: Long): List<MessageReplyDto>? {
        userRepository.findByChatIdAndDeletedFalse(operatorChatId)?.let { operator ->
            sessionRepository.findByOperatorAndActiveTrue(operator)?.let { session ->
                session.active = false
                session.endTime = Date()
                sessionRepository.save(session)
                operator.operatorState = OperatorState.NOT_BUSY
                operator.state = SEND_ANSWER
                userRepository.save(operator)
                return onlineOperator(operatorChatId)
            }
            return null
        } ?: throw OperatorNotFoundException(operatorChatId)
    }


    override fun offlineOperator(operatorChatId: Long) {
        userRepository.findByChatIdAndDeletedFalse(operatorChatId)?.let { operator ->
            sessionRepository.findByOperatorAndActiveTrue(operator)?.let { session ->
                session.active = false
                session.endTime = Date()
                sessionRepository.save(session)
            }
            operator.operatorState = OperatorState.NOT_BUSY
            operator.isOnline = false
            operator.state = STATE_OFFLINE
            userRepository.save(operator)
        } ?: throw OperatorNotFoundException(operatorChatId)
    }

    override fun rateOperator(dto: MarkOperatorDto) {
        dto.run {
            sessionRepository.findByUserChatIdAndRateNull(userChatId)?.let { session ->
                if (mark != null )
                    session.rate = mark
                else
                    session.rate = -1

                sessionRepository.save(session)
            } ?: throw SessionNotFoundException(userChatId)
        }
    }
}

@Service
class MessageServiceImpl(
    private val messageRepository: MessageRepository,
    private val userRepository: UserRepository,
    private val sessionRepository: SessionRepository,
    private val operatorsLanguagesRepository: OperatorsLanguagesRepository,
    private val fileService: FileService
) : MessageService {
    @Transactional
    override fun userWriteMsg(dto: UserMessageDto): OperatorMessageDto? {
        userRepository.findByChatIdAndDeletedFalse(dto.userChatId)?.let {
            var sessions: Sessions? = sessionRepository.findByUserChatIdAndActiveTrue(dto.userChatId)
            var operatorMessageDto: OperatorMessageDto? = null
            if (sessions == null) {
                val newSession = Sessions(it, null, LanguageEnum.valueOf(dto.userLanguage), Date(), null, null, true)
                val operator = userRepository.getOperator(
                    Role.OPERATOR,
                    OperatorState.NOT_BUSY,
                    LanguageEnum.valueOf(dto.userLanguage)
                )
                if (operator != null) {
                    operator.operatorState = OperatorState.BUSY
                    userRepository.save(operator)
                    newSession.operator = operator
                    operatorMessageDto = OperatorMessageDto(dto.body, operator.chatId, null)
                }
                sessions = sessionRepository.save(newSession)
            } else if (sessions.operator != null) {
                operatorMessageDto = OperatorMessageDto(dto.body, sessions.operator!!.chatId, null)
            }
            val message =
                Messages(MessageType.QUESTION, dto.body, false, LanguageEnum.valueOf(dto.userLanguage), it, sessions,FileType.TEXT)
            messageRepository.save(message)
            return operatorMessageDto
        } ?: throw UserNotFoundException(dto.userChatId)
    }

    @Transactional
    override fun userWriteFile(dto: UserFileDto): OperatorFileDto? {
        userRepository.findByChatIdAndDeletedFalse(dto.chatId)?.let { user ->
            var sessions =  sessionRepository.findByUserChatIdAndActiveTrue(dto.chatId)
            var operatorFileDto : OperatorFileDto? = null
            if (sessions == null) {
                val newSession = Sessions(user, null, LanguageEnum.valueOf(dto.userLanguage), Date(), null, null, true)
                val operator = userRepository.getOperator(Role.OPERATOR, OperatorState.NOT_BUSY, LanguageEnum.valueOf(dto.userLanguage))
                if (operator != null) {
                    operator.operatorState = OperatorState.BUSY
                    userRepository.save(operator)
                    newSession.operator = operator
                    operatorFileDto = OperatorFileDto(dto.fileName, dto.caption, operator.chatId,dto.contentType, null)
                }
                sessions =  sessionRepository.save(newSession)
            } else if (sessions.operator != null) {
                operatorFileDto = OperatorFileDto(dto.fileName, dto.caption,sessions.operator!!.chatId, dto.contentType, null)
            }
            var message = Messages(MessageType.QUESTION, dto.caption, false, LanguageEnum.valueOf(dto.userLanguage), user, sessions, FileType.FILE )
            message = messageRepository.save(message)
            fileService.writeFile(FileCreateDto(dto.fileName, basePath, dto.contentType, dto.content), message)
            return operatorFileDto
        } ?: throw UserNotFoundException(dto.chatId)
    }

    override fun operatorWriteFile(dto: UserFileDto): OperatorFileDto? {
        userRepository.findByChatIdAndDeletedFalse(dto.chatId)?.let {operator ->
            val sessions = sessionRepository.findByOperatorChatIdAndActiveTrue(dto.chatId)
            val newMessage = Messages(MessageType.ANSWER, dto.caption, false, LanguageEnum.valueOf(dto.userLanguage), sessions!!.user, sessions, FileType.FILE)
            val message = messageRepository.save(newMessage)
            fileService.writeFile(FileCreateDto(dto.fileName, basePath, dto.contentType, dto.content), message)
            val operatorFileDto = OperatorFileDto(dto.fileName, dto.caption, sessions.user.chatId,dto.contentType,null)
            return operatorFileDto
        } ?: throw OperatorNotFoundException(dto.chatId)
    }

    @Transactional
    override fun operatorWriteMsg(dto: OperatorMessageDto): UserMessageDto {
        userRepository.findByChatIdAndDeletedFalse(dto.operatorChatId)?.let { operator ->
            val session = sessionRepository.findByOperatorChatIdAndActiveTrue(dto.operatorChatId)
            val message = Messages(MessageType.ANSWER,dto.body, false, session!!.chatLanguage, session.user, session,FileType.TEXT)
            messageRepository.save(message)
            val userMessageDto = UserMessageDto(dto.body, session.user.chatId, session.chatLanguage.name)
            return userMessageDto
        } ?: throw RuntimeException()
    }

    override fun getAllMessagesNotRepliedByLanguage(operatorId: Long): List<QuestionsForOperatorDto> {
        val user = userRepository.findByChatIdAndDeletedFalse(operatorId) ?: throw RuntimeException()
        val baseList = mutableListOf<Messages>()
        val list = operatorsLanguagesRepository.getAllLanguagesByOperatorId(user.id!!)
        for (lang in list) {
            val messageList = messageRepository.getNotRepliedMessagesForOperator(lang.name.toString())
            baseList.addAll(messageList)

        }
        return baseList.map { QuestionsForOperatorDto.toDto(it) }
    }


}


@Service
class LanguageServiceImpl(
    private val languageRepository: LanguageRepository
) : LanguageService {
    override fun createLanguage(name: String) {
        languageRepository.existsByName(LanguageEnum.valueOf(name)).runIfFalse {
            languageRepository.save(Languages(LanguageEnum.valueOf(name)))
        }
    }

    override fun updateLanguage(id: Long, dto: LanguageDto) {
        val language = languageRepository.findByIdAndDeletedFalse(id)
        dto.run {
            name.let {
                if (language != null) {
                    language.name = LanguageEnum.valueOf(it)
                }
            }
        }
    }

    override fun getAll(pageable: Pageable) =
        languageRepository.findAllNotDeleted(pageable).map { GetOneLanguageDto.toDto(it) }

    override fun delete(id: Long) {
        languageRepository.trash(id)
    }

    override fun getOneLanguage(id: Long): LanguageDto {
        val language = languageRepository.findByIdAndDeletedFalse(id) ?: throw LanguageNotFoundException(id)
        return LanguageDto(language.name.name)
    }
}

@Service
class OperatorLanguageServiceImp(
    private val repository: OperatorsLanguagesRepository,
    private val userRepository: UserRepository,
    private val languageRepository: LanguageRepository
) : OperatorLanguageService {

    override fun create(dto: OperatorLanguageDto) {
        val operator = userRepository.findByChatIdAndDeletedFalse(dto.operatorChatId)
            ?: throw OperatorNotFoundException(dto.operatorChatId)
        val languages = languageRepository.findByIdAndDeletedFalse(dto.languageId) ?: throw LanguageNotFoundException(
            dto.languageId
        )
        repository.save(OperatorsLanguages(languages, operator))
    }
}

@Service
class FileServiceImp(
    private val fileRepository: FileRepository
): FileService {

    override fun writeFile(fileCreateDto: FileCreateDto, messages: Messages) {
        val file = FileEntity(fileCreateDto.fileName, basePath, fileCreateDto.contentType, messages)
        fileRepository.save(file)
        val outputStream = FileOutputStream(File(basePath + "\\" + fileCreateDto.fileName))
        outputStream.write(fileCreateDto.content)
        outputStream.close()
    }


    override fun readFile(fileName: String): ByteArray {
        val inputStream = FileInputStream(File(basePath + "\\" + fileName))
        val bytes = inputStream.readAllBytes()
        inputStream.close()
        return bytes
    }
}
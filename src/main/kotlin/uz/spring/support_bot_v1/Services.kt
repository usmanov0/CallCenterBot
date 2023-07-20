package uz.spring.support_bot_v1

import jakarta.persistence.EntityManager
import jakarta.transaction.Transactional
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import java.util.Date
import java.util.concurrent.TimeUnit

interface UserService {
    //    fun create(tgUser: ):UserDto
    fun findById(id: Long): UserDto
    fun getAll(pageable: Pageable): Page<UserDto>

    fun addOperator(chatId: Long)

    fun getOperatorsByChatId(chatId: Long): GetOneOperatorDto

    fun getOperators(): List<GetOneOperatorDto>

    fun deleteOperator(chatId: Long)

    fun onlineOperator(operatorChatId: Long) : List<MessageReplyDto>?

    fun closeSession(operatorChatId: Long) : List<MessageReplyDto>?

    fun offlineOperator(operatorChatId: Long)
}

interface MessageService {
    fun userWriteMsg(dto: UserMessageDto) : OperatorMessageDto?
    fun operatorWriteMsg(dto: OperatorMessageDto) : UserMessageDto
    fun findById(id: Long): MessageReplyDto
    fun getAll(pageable: Pageable): Page<MessageReplyDto>
    fun getAllMessagesNotRepliedByLanguage(operatorId: Long): List<QuestionsForOperatorDto>
    fun getAllMessagesBySessionId(sessionId: Long): List<MessageReplyDto>
    fun deliverMessage(userId: Long, messageId: Long): MessageReplyDto
}

interface SessionService {
    //The following methods are implemented in userWriteMsg and operatorWriteMsg methods

    //    fun createChatId(userId: Long, operatorId: Long, chatLanguage: LanguageEnum): Long
//    fun getActiveChatByUserId(userId: Long, active: Boolean = true): Long     //returns sessionId
    fun endSession(operatorId: Long)
}

interface LanguageService {
    fun createLanguage(dto: LanguageDto)

    fun updateLanguage(id: Long, dto: LanguageDto)

    fun getOneLanguage(id: Long): LanguageDto

    fun getAll(pageable: Pageable): Page<GetOneLanguageDto>

    fun delete(id: Long)

}

interface TimeTableService {
    fun operatorStart(operatorId: Long)
    fun findById(timeTableId: Long): TimeTableDto
    fun getAll(pageable: Pageable): Page<TimeTableDto>
    fun operatorFinish(operatorId: Long)
}

interface OperatorLanguageService{
    fun create(dto: OperatorLanguageDto)
}

@Service
class UserServiceImpl(
    private val userRepository: UserRepository,
    private val sessionRepository: SessionRepository,
    private val messageRepository: MessageRepository,
    private val timeTableService: TimeTableService
) : UserService {
    /*   override fun create(dto: UserDto):UserDto {
           if (userRepository.findByChatIdAndDeletedFalse(dto.chatId) != null) throw UserAlreadyExistsException(dto.chatId)
           dto.run { userRepository.save(toEntity()) }
       }*/

    override fun findById(id: Long) = userRepository.findByChatIdAndDeletedFalse(id)?.let { UserDto.toDto(it) }
        ?: throw UserNotFoundException(id)

    override fun getAll(pageable: Pageable): Page<UserDto> =
        userRepository.findAllNotDeleted(pageable).map { UserDto.toDto(it) }

    override fun addOperator(chatId: Long) {
        val user = userRepository.findByChatIdAndDeletedFalse(chatId) ?: throw OperatorNotFoundException(chatId)
        user.role = Role.OPERATOR
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

    override fun onlineOperator(operatorChatId: Long) : List<MessageReplyDto>? {
        val operator = userRepository.findByChatIdAndDeletedFalse(operatorChatId) ?: throw OperatorNotFoundException(operatorChatId)
        var messageList : List<MessageReplyDto>? = null
        sessionRepository.findByOperatorAndActiveTrue(null)?.let {
            it.operator = operator
            sessionRepository.save(it)
            operator.operatorState = OperatorState.BUSY
            userRepository.save(operator)
            timeTableService.operatorStart(operator.id!!)
            messageList = messageRepository.findBySessionIdOrderByCreatedDate(it.id).map { MessageReplyDto.toDto(it) }
        }
        return messageList
    }

    override fun closeSession(operatorChatId: Long): List<MessageReplyDto>? {
        userRepository.findByChatIdAndDeletedFalse(operatorChatId)?.let {operator ->
            sessionRepository.findByOperatorAndActiveTrue(operator)?.let {session ->
                session.active = false
                session.endTime = Date()
                sessionRepository.save(session)
                operator.operatorState = OperatorState.NOT_BUSY
                userRepository.save(operator)
                return onlineOperator(operatorChatId)
            }
            return null
        } ?: throw OperatorNotFoundException(operatorChatId)
    }


    override fun offlineOperator(operatorChatId: Long) {
        userRepository.findByChatIdAndDeletedFalse(operatorChatId)?.let { operator ->
            sessionRepository.findByOperatorAndActiveTrue(operator)?.let {session ->
                session.active = false
                session.endTime = Date()
                sessionRepository.save(session)
            }
            operator.operatorState = OperatorState.NOT_BUSY
            operator.isOnline = false
            userRepository.save(operator)
            timeTableService.operatorFinish(operator.id!!)
        } ?: throw OperatorNotFoundException(operatorChatId)
    }
}
@Service
class MessageServiceImpl(
    private val messageRepository: MessageRepository,
    private val userRepository: UserRepository,
    private val sessionRepository: SessionRepository,
    private val operatorsLanguagesRepository: OperatorsLanguagesRepository
) : MessageService {
    override fun userWriteMsg(dto: UserMessageDto): OperatorMessageDto? {
        userRepository.findByChatIdAndDeletedFalse(dto.userChatId)?.let {
           var sessions: Sessions? = sessionRepository.findByUserChatIdAndActiveTrue(dto.userChatId)
           var operatorMessageDto: OperatorMessageDto? = null
           if (sessions == null) {
               val newSession = Sessions(it, null, LanguageEnum.valueOf(dto.userLanguage), Date(), null, null, true)
               val operator = userRepository.getOperator(Role.OPERATOR, OperatorState.NOT_BUSY, dto.userLanguage)
               if (operator != null) {
                   operator.operatorState = OperatorState.BUSY
                   userRepository.save(operator)
                   newSession.operator = operator
                   operatorMessageDto = OperatorMessageDto(dto.body, operator.chatId, null)
               }
               sessions = sessionRepository.save(newSession)
           }
           val message = Messages(MessageType.QUESTION, dto.body, false, LanguageEnum.valueOf(dto.userLanguage), it, sessions)
           messageRepository.save(message)
           return operatorMessageDto
        } ?: throw UserNotFoundException(dto.userChatId)
    }

    @Transactional
    override fun operatorWriteMsg(dto: OperatorMessageDto) : UserMessageDto {
        userRepository.findByChatIdAndDeletedFalse(dto.operatorChatId)?.let {operator ->
            val session = sessionRepository.findByOperatorChatIdAndActiveTrue(dto.operatorChatId)
            val message = Messages(MessageType.ANSWER, dto.body, false, session!!.chatLanguage, session.user, session)
            messageRepository.save(message)
            val userMessageDto = UserMessageDto(dto.body, session.user.chatId, session.chatLanguage.name)
            return userMessageDto
        } ?: throw RuntimeException()
    }

    override fun findById(id: Long): MessageReplyDto =
        messageRepository.findByIdAndDeletedFalse(id)?.let { MessageReplyDto.toDto(it) }
            ?: throw RuntimeException()

    override fun getAll(pageable: Pageable): Page<MessageReplyDto> =
        messageRepository.findAllNotDeleted(pageable).map { MessageReplyDto.toDto(it) }

    override fun getAllMessagesNotRepliedByLanguage(operatorId: Long): List<QuestionsForOperatorDto> {
        val user = userRepository.findByChatIdAndDeletedFalse(operatorId) ?: throw RuntimeException()
        val baseList = mutableListOf<Messages>()
        val list = operatorsLanguagesRepository.getAllLanguagesByOperatorId(user.id!!)
//        var queryConditions = ""
//        var count = 0
        for (lang in list) {
            val messageList = messageRepository.getNotRepliedMessagesForOperator(lang.name.toString())
            baseList.addAll(messageList)
//            count++
//            when (lang.name.toString()) {
//                "Uzbek" -> {
//                    queryConditions = if (count == 1) queryConditions + "Uzbek"
//                    else "$queryConditions or message_language=Uzbek"
//                }
//
//                "Russian" -> {
//                    queryConditions = if (count == 1) queryConditions + "Russian"
//                    else "$queryConditions or message_language=Russian"
//                }
//
//                "English" -> {
//                    queryConditions = if (count == 1) queryConditions + "English"
//                    else "$queryConditions or message_language=English"
//                }
//            }
        }
//        queryConditions = "$queryConditions)"

        return baseList.map { QuestionsForOperatorDto.toDto(it) }
    }

    override fun getAllMessagesBySessionId(sessionId: Long): List<MessageReplyDto> =
        messageRepository.findAllBySessionId(sessionId).map { MessageReplyDto.toDto(it) }

    override fun deliverMessage(userId: Long, messageId: Long): MessageReplyDto {
        userRepository.findByChatIdAndDeletedFalse(userId) ?: throw UserNotFoundException(userId)
        return messageRepository.findByIdAndDeletedFalse(messageId)?.run { MessageReplyDto(body, createdDate!!) }
            ?: throw MessageNotFoundException(messageId)
    }
}

@Service
class SessionServiceImpl(private val sessionRepository: SessionRepository) : SessionService {
    override fun endSession(operatorId: Long) {
//        val session = sessionRepository.findByUserIdAndActiveTrue(operatorId) ?: throw RuntimeException()
//        session.active = false
//        sessionRepository.save(session)
    }

}

@Service
class TimeTableServiceImp(
    private val timeRepository: TimeRepository,
    private val userRepository: UserRepository,
    private val entityManager: EntityManager
) : TimeTableService {

    override fun operatorStart(operatorId: Long) {
        val operator = operatorId.let {
            userRepository.existsByIdAndDeletedFalse(it).runIfFalse { throw OperatorNotFoundException(it) }
            entityManager.getReference(Users::class.java, it)
        }
        timeRepository.save(TimeTable(Date(), null, null, true, operator))
    }

    override fun findById(timeTableId: Long): TimeTableDto =
        timeRepository.findByIdAndDeletedFalse(timeTableId)?.let { TimeTableDto.toDto(it) }
            ?: throw TimeTableNotFoundException(timeTableId)

    override fun getAll(pageable: Pageable): Page<TimeTableDto> =
        timeRepository.findAllNotDeleted(pageable).map { TimeTableDto.toDto(it) }

    override fun operatorFinish(operatorId: Long) {
        val timeTable =
            timeRepository.findByOperatorIdAndActiveTrue(operatorId) ?: throw TimeTableNotFoundException(operatorId)
        timeTable.endTime = Date()
        val time = timeTable.endTime!!.time - timeTable.startTime.time

        val toHours = TimeUnit.MILLISECONDS.toHours(time)

//        val toMin = TimeUnit.MILLISECONDS.toMinutes(time)
//        val hours = TimeUnit.MILLISECONDS.toHours(durationInMillis)
//    val remainingMinutesInMillis = durationInMillis - TimeUnit.HOURS.toMillis(hours)
//    val minutes = TimeUnit.MILLISECONDS.toMinutes(remainingMinutesInMillis)

        toHours.also { timeTable.totalHours = it.toDouble() }
        timeTable.active = false
        timeRepository.save(timeTable)
    }
}

@Service
class LanguageServiceImpl(
    private val languageRepository: LanguageRepository
) : LanguageService {
    override fun createLanguage(dto: LanguageDto) {
        dto.run {
            languageRepository.save(toEntity())
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
        val operator = userRepository.findByChatIdAndDeletedFalse(dto.operatorChatId) ?: throw OperatorNotFoundException(dto.operatorChatId)
        val languages = languageRepository.findByIdAndDeletedFalse(dto.languageId) ?: throw LanguageNotFoundException(dto.languageId)
        repository.save(OperatorsLanguages(languages, operator))
    }
}

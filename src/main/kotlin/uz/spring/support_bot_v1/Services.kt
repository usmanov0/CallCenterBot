package uz.spring.support_bot_v1

import jakarta.persistence.EntityManager
import jakarta.transaction.Transactional
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.Date
import java.util.concurrent.TimeUnit

interface UserService {
    //    fun create(tgUser: ):UserDto
    fun findById(id: Long): UserDto
    fun getAll(pageable: Pageable): Page<UserDto>
}

interface MessageService {
    fun userWriteMsg(dto: UserMessageDto)
    fun operatorWriteMsg(dto: OperatorMessageDto)
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

interface LanguageService{
    fun createLanguage(dto: LanguageDto)

    fun updateLanguage(id: Long, dto: LanguageDto)

    fun getAll(pageable: Pageable) : Page<GetOneLanguageDto>

    fun delete(id: Long)

}

interface TimeTableService {
    fun operatorStart(operatorId: Long)
    fun findById(timeTableId: Long): TimeTableDto
    fun getAll(pageable: Pageable): Page<TimeTableDto>
    fun operatorFinish(operatorId: Long)
}

@Service
class UserServiceImpl(private val userRepository: UserRepository) : UserService {
    /*   override fun create(dto: UserDto):UserDto {
           if (userRepository.findByChatIdAndDeletedFalse(dto.chatId) != null) throw UserAlreadyExistsException(dto.chatId)
           dto.run { userRepository.save(toEntity()) }
       }*/

    override fun findById(id: Long) = userRepository.findByChatIdAndDeletedFalse(id)?.let { UserDto.toDto(it) }
        ?: throw UserNotFoundException(id)

    override fun getAll(pageable: Pageable): Page<UserDto> =
        userRepository.findAllNotDeleted(pageable).map { UserDto.toDto(it) }
}

@Service
class MessageServiceImpl(
    private val messageRepository: MessageRepository,
    private val userRepository: UserRepository,
    private val sessionRepository: SessionRepository,
    private val operatorsLanguagesRepository: OperatorsLanguagesRepository
) : MessageService {
    override fun userWriteMsg(dto: UserMessageDto) {
        userRepository.findByChatIdAndDeletedFalse(dto.userChatId)?.let {
            dto.run {
                val session = sessionRepository.findByUserIdAndActiveTrue(userChatId)
                messageRepository.save(toEntity(it, session))
            }
        }
    }

    @Transactional
    override fun operatorWriteMsg(dto: OperatorMessageDto) {
        userRepository.findByChatIdAndDeletedFalse(dto.operatorChatId)?.let {
            dto.run {
                val message = messageRepository.findByIdAndDeletedFalse(messageId) ?: throw RuntimeException()
                val user = userRepository.findByChatIdAndDeletedFalse(message.user.chatId)

                val session = sessionRepository.findByUserIdAndActiveTrue(operatorChatId)
                    ?: sessionRepository.save(Sessions(user!!, it, message.messageLanguage, Date(), null, null, true))

                messageRepository.save(toEntity(it, session, message))
                messageRepository.findByIdAndDeletedFalse(messageId)?.let {
                    if (it.session == null) it.session = session
                    it.replied = true
                    messageRepository.save(it)
                }
            }
        } ?: throw RuntimeException()
    }

    override fun findById(id: Long): MessageReplyDto =
        messageRepository.findByIdAndDeletedFalse(id)?.let { MessageReplyDto.toDto(it) }
            ?: throw RuntimeException()

    override fun getAll(pageable: Pageable): Page<MessageReplyDto> =
        messageRepository.findAllNotDeleted(pageable).map { MessageReplyDto.toDto(it) }

    override fun getAllMessagesNotRepliedByLanguage(operatorId: Long): List<QuestionsForOperatorDto> {
        userRepository.findByChatIdAndDeletedFalse(operatorId) ?: throw RuntimeException()
        val list = operatorsLanguagesRepository.getAllLanguagesByOperatorId(operatorId)
        var queryConditions = "where replied=false"
        var count = 0
        for (lang in list) {
            count++
            when (lang.name.toString()) {
                "Uzbek" -> {
                    queryConditions = if (count == 1) "$queryConditions and (message_language=Uzbek"
                    else "$queryConditions or message_language=Uzbek"
                }

                "Russian" -> {
                    queryConditions = if (count == 1) "$queryConditions and (message_language=Russian"
                    else "$queryConditions or message_language=Russian"
                }

                "English" -> {
                    queryConditions = if (count == 1) "$queryConditions and (message_language=English"
                    else "$queryConditions or message_language=English"
                }
            }
        }
        queryConditions = "$queryConditions)"
        return messageRepository.getNotRepliedMessagesForOperator(queryConditions)
            .map { QuestionsForOperatorDto.toDto(it) }
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
        val session = sessionRepository.findByUserIdAndActiveTrue(operatorId) ?: throw RuntimeException()
        session.active = false
        sessionRepository.save(session)
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

@Service
class LanguageServiceImpl(
    private val languageRepository: LanguageRepository
):LanguageService {
    override fun createLanguage(dto: LanguageDto){
        dto.run {
            languageRepository.save(toEntity())
        }
    }
    override fun updateLanguage(id: Long, dto: LanguageDto) {

        val language = languageRepository.findByIdAndDeletedFalse(id)
        dto.run {
            name?.let {
                if (language != null) {
                    language.name = it
                }
            }
        }
    }
    override fun getAll(pageable: Pageable) = languageRepository.findAllNotDeleted(pageable).map { GetOneLanguageDto.toDto(it) }
    override fun delete(id: Long) {
        languageRepository.trash(id)
    }
}
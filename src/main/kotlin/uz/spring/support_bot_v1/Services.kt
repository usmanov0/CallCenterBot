package uz.spring.support_bot_v1

import jakarta.persistence.EntityManager
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import java.util.Date

interface UserService {
    fun create(dto: UserDto)
    fun findById(id: Long): UserDto
    fun getAll(pageable: Pageable): Page<UserDto>
}
interface MessageService {
    fun userWriteMsg(dto: UserMessageDto)
    fun operatorWriteMsg(dto: OperatorMessageDto)
    fun findById(id: Long): MessageReplyDto
    fun getAll(pageable: Pageable): Page<MessageReplyDto>
    fun getAllMessagesNotRepliedByLanguage(operatorId: Long, pageable: Pageable): Page<MessageReplyDto>
    fun deliverMessage(userId: Long)
}


interface ChatService {
    fun createChatId(userId: Long, operatorId: Long, chatLanguage: LanguageEnum): Long
    fun getActiveChatByUserId(userId: Long, active: Boolean=true): Long     //returns sessionId
    fun endSession(sessionId: Long)
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

class UserServiceImpl(private val userRepository: UserRepository) : UserService {
    override fun create(dto: UserDto) {
    }

    override fun findById(id: Long): UserDto {
        TODO("Not yet implemented")
    }

    override fun getAll(pageable: Pageable): Page<UserDto> {
        TODO("Not yet implemented")
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

    override fun findById(timeTableId: Long): TimeTableDto = timeRepository.findByIdAndDeletedFalse(timeTableId)?.let { TimeTableDto.toDto(it) }
        ?: throw TimeTableNotFoundException(timeTableId)

    override fun getAll(pageable: Pageable): Page<TimeTableDto> = timeRepository.findAllNotDeleted(pageable).map { TimeTableDto.toDto(it) }

    override fun operatorFinish(operatorId: Long) {
        val timeTable =
            timeRepository.findByOperatorIdAndActiveTrue(operatorId) ?: throw TimeTableNotFoundException(operatorId)
        timeTable.endTime = Date()
        (timeTable.endTime!!.hours - timeTable.startTime.hours).also { timeTable.totalHours = it.toDouble() }
        timeTable.active = false
        timeRepository.save(timeTable)
    }
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
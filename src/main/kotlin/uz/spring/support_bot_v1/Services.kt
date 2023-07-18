package uz.spring.support_bot_v1

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

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

class UserServiceImpl(private val userRepository: UserRepository) : UserService {
    override fun create(dto: UserDto) {
        TODO("Not yet implemented")
    }

    override fun findById(id: Long): UserDto {
        TODO("Not yet implemented")
    }

    override fun getAll(pageable: Pageable): Page<UserDto> {
        TODO("Not yet implemented")
    }

}
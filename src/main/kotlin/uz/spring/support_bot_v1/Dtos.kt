package uz.spring.support_bot_v1

import org.springframework.data.annotation.CreatedDate
import java.util.Date

data class UserDto(
    val firstName: String?,
    val lastName: String?,
    val accountId: Long,
    val phone: String?,
    val language: LanguageEnum
)

data class MessageReplyDto(
    val body: String,
    val createdDate: Date
)

data class UserMessageDto(
    val body: String,
    val userId: Long,
    val userLanguage: LanguageEnum
)

data class OperatorMessageDto(
    val body: String,
    val operatorId: Long,
    val messageId: Long
)
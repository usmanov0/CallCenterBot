package uz.spring.support_bot_v1

import jakarta.persistence.Column
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import java.util.Date

data class BaseMessage(val code: Int, val message: String?)

data class UserDto(
    val firstName: String,
    val lastName: String?,
    val chatId: Long,
    val phone: String?,
    val language: String,
    val state: String?
) {
    fun toEntity() =
        Users(
            phone,
            chatId,
            Role.USER,
            null,
            mutableSetOf(LanguageEnum.valueOf(language)),
            true,
            state,
            lastName,
            firstName
        )

    companion object {
        fun toDto(user: Users): UserDto {
            return user.run { UserDto(firstName, lastName, chatId, phone, language.toString(), state) }
        }

        /* fun registerUser(tgUser: User) = tgUser.run {
             Users(
                 firstName,
                 lastName,
                 null,
                 id,
                 Role.USER,
                 null,
                 CHOOSE_LANGUAGE,
                 null
             )
         }*/
    }
}


data class MessageReplyDto(
    val body: String,
    val createdDate: Date
) {
    companion object {
        fun toDto(message: Messages) = message.run { MessageReplyDto(body, createdDate!!) }
    }
}

data class UserMessageDto(
    val body: String,
    val userChatId: Long,
    val userLanguage: String
) {
    fun toEntity(user: Users, session: Sessions?) =
        Messages(
            MessageType.QUESTION,
            body,
            false,
            null,
            LanguageEnum.valueOf(userLanguage),
            user,
            session
        )
}

data class OperatorMessageDto(
    val body: String,
    val operatorChatId: Long,
    val replyMessageId: Long?
) {
    fun toEntity(operator: Users, session: Sessions, message: Messages) =
        Messages(
            MessageType.ANSWER,
            body,
            true,
            replyMessageId,
            message.messageLanguage,
            operator,
            session
        )
}

data class QuestionsForOperatorDto(
    val messageId: Long,
    val body: String,
    val msgLanguage: String
) {
    companion object {
        fun toDto(message: Messages) = message.run { QuestionsForOperatorDto(id!!, body, messageLanguage.name) }
    }
}

data class TimeTableDto(
    var startTime: Date,
    var endTime: Date?,
    var totalHours: Double?,
    var active: Boolean,
    val operatorId: Long?,
) {
    companion object {
        fun toDto(timeTable: TimeTable): TimeTableDto {
            return timeTable.run {
                TimeTableDto(startTime, endTime, totalHours, active, operator.id)
            }
        }
    }
}

data class LanguageDto(
    val name: String
) {
    fun toEntity() = Languages(LanguageEnum.valueOf(name))
}

data class LanguageUpdateDto(
    val name: String
)


data class GetOneLanguageDto(
    val name: LanguageEnum
) {
    companion object {
        fun toDto(language: Languages): GetOneLanguageDto {
            return language.run {
                GetOneLanguageDto(name)
            }
        }
    }
}

data class GetOneOperatorDto    (
    val id: Long?,
    val phone: String?,
    val chatId: Long,
    val operatorState: String,
    var role: String,
    var language: MutableSet<LanguageEnum>?,
    var state: String?,
    var isOnline: Boolean?,
    var lastName: String?,
    var firstName: String
){

    companion object {
        fun toDo(operator: Users) : GetOneOperatorDto{
            return operator.run {
                GetOneOperatorDto(id, phone, chatId, role.name, operatorState!!.name, language,state, isOnline, lastName, firstName)
            }
        }
    }
}

data class OperatorLanguageDto(
    val operatorChatId: Long,
    val languageId: Long
)
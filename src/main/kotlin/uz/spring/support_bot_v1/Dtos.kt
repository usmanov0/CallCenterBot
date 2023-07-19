package uz.spring.support_bot_v1

import java.util.Date

data class BaseMessage(val code: Int, val message: String?)

data class UserDto(
    val firstName: String,
    val lastName: String?,
    val chatId: Long,
    val phone: String?,
    val language: String
) {
    fun toEntity() =
        Users(
            firstName,
            lastName,
            phone,
            chatId,
            Role.USER,
            null,
            CHOOSE_LANGUAGE,
            mutableSetOf(LanguageEnum.valueOf(language))
        )

    companion object {
        fun toDto(user: Users): UserDto {
            return user.run { UserDto(firstName, lastName, chatId, phone, language.toString()) }
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
    val replyMessageId: Long,
    val telegramMessageId: Int
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
    val name: LanguageEnum
) {
    fun toEntity() = Languages(name)
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
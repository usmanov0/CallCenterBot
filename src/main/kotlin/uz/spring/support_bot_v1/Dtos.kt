package uz.spring.support_bot_v1

import java.util.*

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
            LanguageEnum.valueOf(language),
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
    val body: String?,
    val fileDto: FileResponseDto?
)

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
            LanguageEnum.valueOf(userLanguage),
            user,
            session,
            FileType.TEXT
        )
}

data class UserFileDto(
    val fileName: String,
    val caption: String?,
    val contentType: String,
    val userChatId: Long,
    val userLanguage: String,
    val content: ByteArray
) {
    fun toEntity(user: Users, session: Sessions?) =
        Messages(
            MessageType.QUESTION,
            fileName,
            false,
            LanguageEnum.valueOf(userLanguage),
            user,
            session,
            FileType.FILE
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
            message.messageLanguage,
            operator,
            session,
            FileType.TEXT
        )
}

data class QuestionsForOperatorDto(
    val messageId: Long,
    val body: String?,
    val msgLanguage: String
) {
    companion object {
        fun toDto(message: Messages) = message.run { QuestionsForOperatorDto(id!!, body, messageLanguage.name) }
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

data class GetOneOperatorDto(
    val id: Long?,
    val phone: String?,
    val chatId: Long,
    val operatorState: String,
    var role: String,
    var language: LanguageEnum?,
    var state: String?,
    var isOnline: Boolean?,
    var lastName: String?,
    var firstName: String
) {

    companion object {
        fun toDo(operator: Users): GetOneOperatorDto {
            return operator.run {
                GetOneOperatorDto(
                    id,
                    phone,
                    chatId,
                    role.name,
                    operatorState!!.name,
                    language,
                    state,
                    isOnline,
                    lastName,
                    firstName
                )
            }
        }
    }
}

data class OperatorLanguageDto(
    val operatorChatId: Long,
    val languageId: Long
)

data class FileCreateDto(
    var fileName: String,
    var path: String,
    var contentType: String,
    var content: ByteArray

) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as FileCreateDto

        if (fileName != other.fileName) return false
        if (path != other.path) return false
        if (contentType != other.contentType) return false
        if (!content.contentEquals(other.content)) return false

        return true
    }
    override fun hashCode(): Int {
        var result = fileName.hashCode()
        result = 31 * result + path.hashCode()
        result = 31 * result + contentType.hashCode()
        result = 31 * result + content.contentHashCode()
        return result
    }
}

data class OperatorFileDto(
    val fileName: String,
    val caption: String?,
    val contentType: String,
    val operatorChatId: Long,
    val content: ByteArray?,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as OperatorFileDto

        if (fileName != other.fileName) return false
        if (caption != other.caption) return false
        if (contentType != other.contentType) return false
        if (operatorChatId != other.operatorChatId) return false
        if (!content.contentEquals(other.content)) return false

        return true
    }
    override fun hashCode(): Int {
        var result = fileName.hashCode()
        result = 31 * result + (caption?.hashCode() ?: 0)
        result = 31 * result + contentType.hashCode()
        result = 31 * result + operatorChatId.hashCode()
        result = 31 * result + content.contentHashCode()
        return result
    }
}

data class FileResponseDto(
    var fileName: String,
    var path: String,
    var contentType: String,
    var content: ByteArray,

) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as FileResponseDto

        if (fileName != other.fileName) return false
        if (path != other.path) return false
        if (contentType != other.contentType) return false
        if (!content.contentEquals(other.content)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = fileName.hashCode()
        result = 31 * result + path.hashCode()
        result = 31 * result + contentType.hashCode()
        result = 31 * result + content.contentHashCode()
        return result
    }

}



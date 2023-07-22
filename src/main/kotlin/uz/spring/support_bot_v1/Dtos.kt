package uz.spring.support_bot_v1

data class BaseMessage(val code: Int, val message: String?)

data class UserDto(
    val firstName: String,
    val lastName: String?,
    val chatId: Long,
    val phone: String?,
    val language: String,
    val state: String?
) {

    companion object {
        fun toDto(user: Users): UserDto {
            return user.run { UserDto(firstName, lastName, chatId, phone, language.toString(), state) }
        }
    }
}


data class MessageReplyDto(
    val body: String?,
    val fileDto: FileResponseDto?
)

data class RequestMessageDto(
    val userChatId: Long,
    val body: String,
    val userLanguage: String?,  // null for operator
    val messageTgId: Long,
    val repliedMessageTgId: Long?,  // generated id by tg for the replied message
)

data class ResponseMessageDto(
    val userChatId: Long,
    val messageBody: String,
    val messageId: Long,
    val repliedMessageTgId: Long?,  // generated id by tg for the replied message
)


data class UserFileDto(
    val fileName: String,
    val caption: String?,
    val contentType: String,
    val chatId: Long,
    val userLanguage: String,
    val content: ByteArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as UserFileDto

        if (fileName != other.fileName) return false
        if (caption != other.caption) return false
        if (contentType != other.contentType) return false
        if (chatId != other.chatId) return false
        if (userLanguage != other.userLanguage) return false
        if (!content.contentEquals(other.content)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = fileName.hashCode()
        result = 31 * result + (caption?.hashCode() ?: 0)
        result = 31 * result + contentType.hashCode()
        result = 31 * result + chatId.hashCode()
        result = 31 * result + userLanguage.hashCode()
        result = 31 * result + content.contentHashCode()
        return result
    }
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
}



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
data class MarkOperatorDto(
    val userChatId: Long,
    val mark: Short?
)
data class OperatorFileDto(
    val fileName: String,
    val caption: String?,
    val chatId: Long,
    val contentType: String,
    val content: ByteArray?,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as OperatorFileDto

        if (fileName != other.fileName) return false
        if (caption != other.caption) return false
        if (contentType != other.contentType) return false
        if (chatId != other.chatId) return false

        return true
    }
    override fun hashCode(): Int {
        var result = fileName.hashCode()
        result = 31 * result + (caption?.hashCode() ?: 0)
        result = 31 * result + contentType.hashCode()
        result = 31 * result + chatId.hashCode()
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



package uz.spring.support_bot_v1

import jakarta.persistence.ManyToOne
import org.springframework.data.annotation.CreatedDate
import java.util.Date

data class BaseMessage(val code: Int, val message: String?)

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

data class TimeTableDto(
    var startTime: Date,
    var endTime: Date?,
    var totalHours: Double?,
    var active: Boolean,
    val operatorId: Long?,
) {
    companion object {
        fun toDto(timeTable: TimeTable) : TimeTableDto {
            return timeTable.run {
                TimeTableDto(startTime, endTime, totalHours, active, operator.id)
            }
        }
    }
}

data class LanguageDto(
    val name: LanguageEnum
){
    fun toEntity()  = Languages(name)
}

data class LanguageUpdateDto(
    val name: String
)


data class GetOneLanguageDto(
    val  name: LanguageEnum
){
    companion object{
        fun toDto(language: Languages):  GetOneLanguageDto{
            return language.run {
                GetOneLanguageDto(name)
            }
        }
    }
}
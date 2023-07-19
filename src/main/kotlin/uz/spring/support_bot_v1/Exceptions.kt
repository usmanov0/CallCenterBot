package uz.spring.support_bot_v1

import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.context.support.ResourceBundleMessageSource
import java.util.*

sealed class DemoException(message: String? = null): RuntimeException(message) {
    abstract fun errorType(): ErrorCode
    fun getErrorMessage(): BaseMessage{
        return BaseMessage(
            errorType().code,
            errorType().name
        )
    }
}


class UserNotFoundException(val id: Long) : DemoException() {
    override fun errorType() = ErrorCode.USER_NOT_FOUND
}
class UserAlreadyExistsException(val id: Long) : DemoException() {
    override fun errorType() = ErrorCode.USER_ALREADY_EXISTS
}

class MessageNotFoundException(val id: Long) : DemoException() {
    override fun errorType() = ErrorCode.MESSAGE_NOT_FOUND
}

class OperatorNotFoundException(val id: Long) : DemoException(ErrorCode.OPERATOR_NOT_FOUND.name) {
    override fun errorType() = ErrorCode.OPERATOR_NOT_FOUND
}

class TimeTableNotFoundException(val id: Long) : DemoException(ErrorCode.TIME_TABLE_NOT_FOUND.name) {
    override fun errorType() = ErrorCode.TIME_TABLE_NOT_FOUND
}

class LanguageNotFoundException(val id: Long) : DemoException() {
    override fun errorType() = ErrorCode.LANGUAGE_NOT_FOUND
}
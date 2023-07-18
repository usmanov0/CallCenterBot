package uz.spring.support_bot_v1

sealed class DemoException(message: String? = null): RuntimeException(message) {
    abstract fun errorType(): ErrorCode
    fun getErrorMessage(): BaseMessage = BaseMessage(errorType().code, message)
}

class OperatorNotFoundException(val id: Long) : DemoException(ErrorCode.OPERATOR_NOT_FOUND.name) {
    override fun errorType() = ErrorCode.OPERATOR_NOT_FOUND
}

class TimeTableNotFoundException(val id: Long) : DemoException(ErrorCode.TIME_TABLE_NOT_FOUND.name) {
    override fun errorType() = ErrorCode.TIME_TABLE_NOT_FOUND
}
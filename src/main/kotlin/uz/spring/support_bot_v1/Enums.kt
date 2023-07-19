package uz.spring.support_bot_v1

enum class LanguageEnum {
    Uzbek, English, Russian
}

enum class MessageType {
    QUESTION, ANSWER
}

enum class Role {
    USER, OPERATOR
}

enum class ErrorCode(val code: Int) {
    OPERATOR_NOT_FOUND(100),
    TIME_TABLE_NOT_FOUND(101),
    USER_NOT_FOUND(102),
    MESSAGE_NOT_FOUND(103),
    USER_ALREADY_EXISTS(104)
}
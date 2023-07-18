package uz.spring.support_bot_v1

data class UserDto(
    val firstName: String?,
    val lastName: String?,
    val accountId: Long,
    val phone: String?,
    val language: LanguageEnum
)
package uz.spring.support_bot_v1

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.bind.ConstructorBinding

@ConfigurationProperties(prefix = "telegram-bot")
class TelegramBotProperties @ConstructorBinding constructor(
    var username: String,
    var token: String
)
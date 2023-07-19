package uz.spring.support_bot_v1

import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component

@Component
class DatabaseInitializer(private val userRepository: UserRepository) : CommandLineRunner {

    override fun run(vararg args: String?) {
//         Your user creation logic goes here
//        createUsers()
    }

    private fun createUsers() {
//        userRepository.findByChatId()
        val language = LanguageEnum.valueOf(UZBEK)
        val language1 = LanguageEnum.valueOf(ENGLISH)
        val user1 =
            Users(
                "Ulug'bek",
                "online",
                "901362576",
                OPERATOR_CHAT_ID1,
                Role.OPERATOR,
                true,
                OperatorState.NOT_BUSY.name,
                mutableSetOf(language, language1),
            )
        val user2 = Users(
            "Aziz",
            "Aziz",
            "995611060",
            OPERATOR_CHAT_ID2,
            Role.OPERATOR,
            true,
            OperatorState.NOT_BUSY.name,
            mutableSetOf(language)
        )

        userRepository.saveAll(listOf(user1, user2))
    }
}

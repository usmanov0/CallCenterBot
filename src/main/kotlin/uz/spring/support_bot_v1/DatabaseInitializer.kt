package uz.spring.support_bot_v1

import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component

@Component
class DatabaseInitializer(private val userRepository: UserRepository) : CommandLineRunner {

    override fun run(vararg args: String?) {
        // Your user creation logic goes here
//        createUsers()
    }

//    private fun createUsers() {
////        userRepository.findByChatId()
//        val user1 = User("Ulug'bek", "online", "901362576", OPERATOR_CHAT_ID1, OPERATOR, true, "active")
//        val user2 = User("Aziz", "Aziz", "995611060", OPERATOR_CHAT_ID2, OPERATOR, true, "active")
//
//        userRepository.saveAll(listOf(user1, user2))
//    }
}

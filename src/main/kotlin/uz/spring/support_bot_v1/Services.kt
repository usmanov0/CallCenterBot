package uz.spring.support_bot_v1

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface UserService {
    fun create(dto: UserDto)
    fun findById(id: Long): UserDto
    fun getAll(pageable: Pageable): Page<UserDto>
}

class UserServiceImpl(private val userRepository: UserRepository) : UserService {
    override fun create(dto: UserDto) {
        TODO("Not yet implemented")
    }

    override fun findById(id: Long): UserDto {
        TODO("Not yet implemented")
    }

    override fun getAll(pageable: Pageable): Page<UserDto> {
        TODO("Not yet implemented")
    }

}
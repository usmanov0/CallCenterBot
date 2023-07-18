package uz.spring.support_bot_v1

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface UserService {
    fun create(dto: CreateUserDto)
    fun findById(id: Long): GetUserDto
    fun getAll(pageable: Pageable): Page<GetUserDto>
    fun update(id: Long, dto: UpdateUserDto)
    fun delete(id: Long)
}

class Services {
}
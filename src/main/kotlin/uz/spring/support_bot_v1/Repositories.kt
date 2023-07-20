package uz.spring.support_bot_v1

import jakarta.persistence.EntityManager
import jakarta.transaction.Transactional
import org.apache.catalina.User
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.domain.Specification
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Query
import org.springframework.data.jpa.repository.support.JpaEntityInformation
import org.springframework.data.jpa.repository.support.SimpleJpaRepository
import org.springframework.data.repository.NoRepositoryBean
import org.springframework.data.repository.findByIdOrNull
import org.springframework.data.repository.query.Param

@NoRepositoryBean
interface BaseRepository<T : BaseEntity> : JpaRepository<T, Long>, JpaSpecificationExecutor<T> {
    fun findByIdAndDeletedFalse(id: Long): T?
    fun trash(id: Long): T?
    fun trashList(ids: List<Long>): List<T?>
    fun findAllNotDeleted(): List<T>
    fun findAllNotDeleted(pageable: Pageable): Page<T>
}

class BaseRepositoryImpl<T : BaseEntity>(
    entityInformation: JpaEntityInformation<T, Long>, entityManager: EntityManager,
) : SimpleJpaRepository<T, Long>(entityInformation, entityManager), BaseRepository<T> {

    val isNotDeletedSpecification = Specification<T> { root, _, cb -> cb.equal(root.get<Boolean>("deleted"), false) }

    override fun findByIdAndDeletedFalse(id: Long) = findByIdOrNull(id)?.run { if (deleted) null else this }

    @Transactional
    override fun trash(id: Long): T? = findByIdOrNull(id)?.run {
        deleted = true
        save(this)
    }

    override fun findAllNotDeleted(): List<T> = findAll(isNotDeletedSpecification)
    override fun findAllNotDeleted(pageable: Pageable): Page<T> = findAll(isNotDeletedSpecification, pageable)
    override fun trashList(ids: List<Long>): List<T?> = ids.map { trash(it) }
}

interface UserRepository : BaseRepository<Users> {
    fun findByChatIdAndDeletedFalse(chatId: Long): Users?
//    fun findByAccountId(chatId: Long): Users?

    fun findAllByRoleAndDeletedFalse(role: Role): List<Users>

    fun existsByIdAndDeletedFalse(id: Long): Boolean

    /* @Query(value = "select o from users o inner " +
             "join OperatorsLanguages ol on o.id = ol.operator.id where " +
             "o.role = :role and o.isOnline = true and o.operatorState = :state and ol.language.name = :language limit 1")*/
    @Query(
        value = "select o from Users o inner join OperatorsLanguages ol on o.id = ol.operator.id " +
                "where o.role = :role and o.isOnline = true and o.operatorState = :state and ol.language.name = :language"
    )
    fun getOperator(
        @Param("role") role: Role,
        @Param("state") state: OperatorState,
        @Param("language") languages: LanguageEnum
    ): Users?
}

interface SessionRepository : BaseRepository<Sessions> {
    fun findByUserChatIdAndActiveTrue(userChatId: Long): Sessions?
    fun findByOperatorIdAndActiveTrue(operatorId: Long): Sessions?
    fun findByOperatorChatIdAndActiveTrue(operatorChatId: Long): Sessions?


    @Query(value = "select s from Sessions s where s.chatLanguage = :language and s.operator = null and s.active = true")
    fun getSession(@Param("language") languages: LanguageEnum): List<Sessions>
    fun findByOperatorAndActiveTrue(operator: Users): Sessions?

    // Every session has non-null rate for finished ones, and null rate for active ones
    fun findByUserChatIdAndRateNull(userChatId: Long): Sessions?
}

interface MessageRepository : BaseRepository<Messages> {
    @Query(value = "select * from messages where replied=false and (message_language=?1)", nativeQuery = true)
    fun getNotRepliedMessagesForOperator(conditions: String): List<Messages>

    fun findBySessionIdOrderByCreatedDate(sessionId: Long?): List<Messages>
    fun findAllBySessionId(sessionId: Long): List<Messages>
}

interface LanguageRepository : BaseRepository<Languages> {
    fun existsByName(name: LanguageEnum): Boolean
}

interface OperatorsLanguagesRepository : BaseRepository<OperatorsLanguages> {
    @Query(
        value = "select l from OperatorsLanguages as ol join Languages as l on ol.language.id=l.id " +
                "where ol.operator.id=?1"
    )
    fun getAllLanguagesByOperatorId(operatorId: Long): List<Languages>
}

interface FileRepository : BaseRepository<FileEntity> {
    fun findByMessagesId(messageId: Long): FileEntity
}

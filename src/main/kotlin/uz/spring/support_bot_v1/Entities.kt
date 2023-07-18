package uz.spring.support_bot_v1

import jakarta.persistence.*
import org.hibernate.annotations.ColumnDefault
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import java.util.*

@MappedSuperclass
class BaseEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) var id: Long? = null,
    @CreatedDate @Temporal(TemporalType.TIMESTAMP) var createdDate: Date? = Date(),
    @LastModifiedDate @Temporal(TemporalType.TIMESTAMP) var modifiedDate: Date? = Date(),
    @Column(nullable = false) @ColumnDefault(value = "false") var deleted: Boolean = false
)

@Entity(name = "users")
class Users(
    var firstName: String,
    var lastName: String,
    @Column(length = 50, unique = true) var phone: String,
    @Column(unique = true) var chatId: Long?,
    var role: String,
    var isOnline: Boolean,
    var state: String,
    var language: MutableList<LanguageEnum>?
) : BaseEntity()

@Entity
class MyChat(
    var chatLanguage: String,
    var startTime: Date,
    var endTime: Date?,
    var rate: Short?,
    @ManyToOne var users: Users,
    @ManyToOne var operator: Users
) : BaseEntity()

@Entity
class MyMessage(
    var type: String,
    var body: String,
    @ManyToOne var users: Users,
    @ManyToOne var chat: MyChat
) : BaseEntity()

@Entity
class TimeTable(
    var startTime: Date,
    var endTime: Date?,
    var totalHours: Int?,
    @ManyToOne var operator: Users
) : BaseEntity()

@Entity
class Language(
    var name: LanguageEnum,
) : BaseEntity()

@Entity
class OperatorsLanguage(
    @ManyToOne var languages: Language,
    @ManyToOne var operator: Users
) : BaseEntity()


enum class LanguageEnum(
    /*private var uz: String,
    private var ru: String,
*/
    private var ll: String
) {
    UZBEK("uz"), ENGLISH("en"), RUSSIAN("ru")

}
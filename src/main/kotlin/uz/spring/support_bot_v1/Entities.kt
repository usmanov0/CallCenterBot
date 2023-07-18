package uz.spring.support_bot_v1

import jakarta.persistence.*
import org.hibernate.annotations.ColumnDefault
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
    var lastName: String?,
    @Column(length = 50, unique = true) var phone: String?,
    @Column(unique = true) var accountId: Long,
    var role: String,
    var isOnline: Boolean?,  // for operator not null, for user always null
    var state: String?,     // for user
    @Enumerated(EnumType.STRING)
    @ElementCollection(targetClass = LanguageEnum::class, fetch = FetchType.EAGER)
    var language: MutableSet<LanguageEnum>?
) : BaseEntity()

@Entity
class Sessions(
    @ManyToOne var user: Users,
    @ManyToOne var operator: Users,
    var chatLanguage: String,
    var startTime: Date,
    var endTime: Date?,
    var rate: Short?,
    var active: Boolean
) : BaseEntity()

@Entity
class Messages(
    @Enumerated(EnumType.STRING)
    var type: MessageType,
    var body: String,
    var replied: Boolean,
    var messageLanguage: LanguageEnum,
    @ManyToOne var user: Users,
    @ManyToOne var session: Sessions,
    @OneToOne var message: Messages?,
) : BaseEntity()

@Entity
class TimeTable(
    var startTime: Date,
    var endTime: Date?,
    var totalHours: Double?,
    var active: Boolean,
    @ManyToOne var operator: Users,
    ) : BaseEntity()

@Entity
class Languages(
    var name: LanguageEnum,
) : BaseEntity()

@Entity
class OperatorsLanguages(
    @ManyToOne var language: Languages,
    @ManyToOne var operator: Users
) : BaseEntity()

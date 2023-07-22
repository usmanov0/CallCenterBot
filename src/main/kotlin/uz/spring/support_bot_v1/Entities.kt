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

@Entity()
class Users(
    @Column(length = 50, unique = true) var phone: String?,
    @Column(unique = true) var chatId: Long,
    @Enumerated(EnumType.STRING) var role: Role,
    @Enumerated(EnumType.STRING) var operatorState: OperatorState?,
    @Enumerated(EnumType.STRING)
    var language: LanguageEnum?,
    var isOnline: Boolean = false,
    var state: String?,
    var lastName: String?,
    var firstName: String
) : BaseEntity()

@Entity
class Sessions(
    @ManyToOne var user: Users,
    @ManyToOne var operator: Users?,
    @Enumerated(EnumType.STRING) var chatLanguage: LanguageEnum,
    var startTime: Date,
    var endTime: Date?,
    var rate: Short?,
    var active: Boolean
) : BaseEntity()

@Entity
class Messages(
    @Enumerated(EnumType.STRING)
    var type: MessageType,
    var body: String?,
    var tgMessageId4User: Long?,
    var tgMessageId4Oper: Long?,
    var replied: Boolean,
    @Enumerated(EnumType.STRING)
    var messageLanguage: LanguageEnum,
    @ManyToOne
    var user: Users,
    @ManyToOne
    var session: Sessions?,
    @Enumerated(EnumType.STRING)
    var fileType: FileType,
    @ManyToOne
    var repliedMessageId: Messages?
) : BaseEntity()

@Entity
class Languages(
    @Enumerated(value = EnumType.STRING)
    var name: LanguageEnum,
) : BaseEntity()


@Entity
class OperatorsLanguages(
    @ManyToOne var language: Languages,
    @ManyToOne var operator: Users
) : BaseEntity()

@Entity
class FileEntity(
    var fileName: String,
    var path: String,
    var contentType: String,
    @OneToOne var messages: Messages
) : BaseEntity()

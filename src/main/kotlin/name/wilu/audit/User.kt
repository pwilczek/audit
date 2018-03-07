package name.wilu.audit

import java.util.*
import javax.persistence.Entity
import javax.persistence.EntityManager
import javax.persistence.Id
import javax.persistence.PersistenceContext

internal open class UserAuditor : Auditor {
    //
    @PersistenceContext
    lateinit var em: EntityManager

    //
    override fun process(record: AuditRecord) = em.run {
        persist(record)
    }
}

@Entity
internal class User {
    @Id var id: UUID? = null
    //
    @Audited(UserAuditor::class)
    lateinit var name: String
    //
    lateinit var surname: String
}
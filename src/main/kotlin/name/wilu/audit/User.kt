package name.wilu.audit

import org.springframework.transaction.annotation.Transactional
import java.util.*
import javax.persistence.Entity
import javax.persistence.EntityManager
import javax.persistence.Id
import javax.persistence.PersistenceContext

internal class UserAuditor : Auditor {
    //
    @PersistenceContext lateinit var  em : EntityManager
    //
    @Transactional
    override fun process(records: List<AuditRecord>) = records.forEach { em.persist(it) }
}

@Entity
internal class User {
    @Id var id: UUID? = null
    //
    @Audited(processor = UserAuditor::class)
    lateinit var name: String
    //
    lateinit var surname: String
}
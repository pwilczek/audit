package name.wilu.audit

import java.util.*
import javax.persistence.Entity
import javax.persistence.Id

@Entity
internal class User {
    @Id var id: UUID? = null
    lateinit var name: String
    lateinit var surname: String
}
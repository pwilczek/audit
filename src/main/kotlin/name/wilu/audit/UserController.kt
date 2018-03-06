package name.wilu.audit

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController
import java.util.UUID.randomUUID
import javax.persistence.EntityManager
import javax.persistence.PersistenceContext

@RestController
internal class UserController(private val service: UserService) {
    //
    // curl -H "Content-Type: application/json" -X POST -d '{"name":"js","surname":"smith"}' http://localhost:8080/api/users -v
    @PostMapping("api/users")
    @ResponseBody
    fun add(@RequestBody user: User) = service.add(user)
}

@Service
@Transactional
internal class UserService(@PersistenceContext private val em: EntityManager) {
    //
    fun add(user: User): User = user.let {
        it.id= randomUUID()
        em.persist(it)
        return it
    }
}
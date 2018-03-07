package name.wilu.audit

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.*
import java.util.*
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

    // curl -H "Content-Type: application/json" -X POST -d '{"name":"js","surname":"smith"}' http://localhost:8080/api/users/{id} -v
    @PostMapping("api/users/{id}")
    @ResponseBody
    fun update(@PathVariable id: UUID, @RequestBody user: User) = user.apply {
        this.id = id
        service.update(this)
    }
}

@Service
@Transactional
internal open class UserService(@PersistenceContext private val em: EntityManager) {
    //
    open fun add(user: User): User = user.let {
        it.id = randomUUID()
        em.persist(it)
        return it
    }

    open fun update(user: User): User = em.find(User::class.java, user.id).apply {
        name = user.name
        surname = user.surname
    }
}
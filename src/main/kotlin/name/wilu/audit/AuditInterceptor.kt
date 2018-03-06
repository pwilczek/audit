package name.wilu.audit

import org.hibernate.EmptyInterceptor
import org.hibernate.Transaction
import org.hibernate.type.Type
import org.springframework.jms.core.JmsTemplate
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import java.io.Serializable
import java.util.*
import javax.persistence.Entity
import javax.persistence.Id
import kotlin.annotation.AnnotationRetention.RUNTIME
import kotlin.reflect.KClass

@Retention(RUNTIME)
@Target(AnnotationTarget.FIELD)
internal annotation class Audited(val processor: KClass<out Auditor>)

internal interface Auditor {
    fun process(records: List<out AuditRecord>)
}

@Entity
internal class AuditRecord(@Id val id: UUID) {
    //
    lateinit var topic: UUID
    var oldVal: String? = null
    var newVal: String? = null
    //
    fun old(oldVal: String?) = apply { this.oldVal = oldVal }
    fun new(newVal: String?) = apply { this.newVal = newVal }
    //
    companion object {
        fun of(topic: Any) = AuditRecord(UUID.randomUUID()).apply {
            if (topic is UUID) this.topic = topic
            else throw IllegalArgumentException()
        }
    }
}

internal class AuditInterceptor(private val jsm : JmsTemplate) : EmptyInterceptor() {

    private val commands: MultiValueMap<KClass<out Auditor>, AuditRecord> = LinkedMultiValueMap()

    override fun onSave(entity: Any?,
                        id: Serializable?,
                        state: Array<out Any>?,
                        propertyNames: Array<out String>?,
                        types: Array<out Type>?): Boolean {
        //

        entity!!::class.java.declaredFields
                .filter { it.isAnnotationPresent(Audited::class.java) }
                .forEach {
                    commands.add(
                            it.getAnnotation(Audited::class.java).processor,
                            AuditRecord.of(id as Any).old(null).new(it.get(entity).toString())
                    )
                }
        return false
    }

    override fun afterTransactionCompletion(tx: Transaction?) {
        if (!tx!!.rollbackOnly) commands.keys.forEach {
            SpringInitializer.initialize(it).process(commands[it] as List<AuditRecord>)
        }
    }
}
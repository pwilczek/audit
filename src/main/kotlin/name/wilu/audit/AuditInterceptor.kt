package name.wilu.audit

import org.hibernate.EmptyInterceptor
import org.hibernate.Transaction
import org.hibernate.internal.util.compare.EqualsHelper.areEqual
import org.hibernate.type.Type
import org.springframework.jms.core.JmsTemplate
import java.io.Serializable
import java.lang.reflect.Field
import java.util.*
import java.util.UUID.randomUUID
import javax.persistence.Entity
import javax.persistence.Id
import kotlin.annotation.AnnotationRetention.RUNTIME
import kotlin.reflect.KClass

@Retention(RUNTIME)
@Target(AnnotationTarget.FIELD)
internal annotation class Audited(val value: KClass<out Auditor>)

internal interface Auditor {
    fun process(record: AuditRecord)
}

internal object NoOpAuditor : Auditor {
    override fun process(record: AuditRecord) {}
}

@Entity
internal class AuditRecord {
    //
    @Id lateinit var id: UUID
    lateinit var topic: UUID
    var old: String? = null
    var new: String? = null
    @Transient var auditor: String? = NoOpAuditor::class.qualifiedName // needs custom serializer
    //
    fun old(oldVal: String?) = apply { old = oldVal }
    fun new(newVal: String?) = apply { new = newVal }
    fun auditor(auditor: String?) = apply { this.auditor = auditor }
    //
    companion object {
        fun of(topic: Any) = AuditRecord().apply {
            id = randomUUID()
            if (topic is UUID) this.topic = topic
            else throw IllegalArgumentException()
        }
    }
}

internal class AuditInterceptor(private val jms : JmsTemplate) : EmptyInterceptor() {
    //
    private val commands: MutableList<AuditRecord> = mutableListOf() // should be tx scoped

    override fun onSave(entity: Any,
                        id: Serializable,
                        state: Array<out Any>,
                        propertyNames: Array<out String>,
                        types: Array<out Type>): Boolean {
        //
        auditedFields(entity).forEach {
            commands.add(record(id as Any, null, it.get(entity).toString(), it))
        }
        return false
    }

    override fun onFlushDirty(entity: Any,
                              id: Serializable,
                              currentState: Array<out Any>,
                              previousState: Array<out Any>,
                              propertyNames: Array<out String>,
                              types: Array<out Type>?): Boolean {
        auditedFields(entity).forEach {
            val auditRequired = auditRequired(it, previousState, currentState, propertyNames)
            if (auditRequired.first) commands.add(record(id as Any, auditRequired.second, it.get(entity).toString(), it))
        }
        return false
    }

    private fun auditRequired(field: Field, prv: Array<out Any>, current: Array<out Any>, props: Array<out String>): Pair<Boolean, String?> {
        val index = props.indexOf(field.name)
        val old = prv[index]
        return if (areEqual(old, current[index])) Pair(false, null) else Pair(true, old.toString()) // old might be null
                                                                                                    // maybe optional
    }

    private fun auditedFields(entity: Any?) = entity!!::class.java.declaredFields
            .filter { it.isAnnotationPresent(Audited::class.java) }

    private fun record(topic: Any, oldVal: String?, newVal: String, it: Field) = AuditRecord
            .of(topic)
            .old(oldVal)
            .new(newVal)
            .auditor(it.getAnnotation(Audited::class.java).value.qualifiedName)

    override fun afterTransactionCompletion(tx: Transaction?) {
        if (!tx!!.rollbackOnly) commands.forEach { jms.convertAndSend(AUDIT_QUEUE, it) }
        commands.clear()
    }
}
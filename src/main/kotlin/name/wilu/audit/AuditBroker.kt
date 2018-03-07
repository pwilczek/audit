package name.wilu.audit

import org.springframework.boot.autoconfigure.jms.DefaultJmsListenerContainerFactoryConfigurer
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.jms.annotation.EnableJms
import org.springframework.jms.annotation.JmsListener
import org.springframework.jms.config.DefaultJmsListenerContainerFactory
import org.springframework.jms.support.converter.MappingJackson2MessageConverter
import org.springframework.jms.support.converter.MessageType.TEXT
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import javax.jms.ConnectionFactory
import kotlin.reflect.full.createInstance


@EnableJms
@Configuration
internal open class AuditBroker {

    @Bean
    open fun auditJmsFactory(cf: ConnectionFactory, cfer: DefaultJmsListenerContainerFactoryConfigurer) =
            DefaultJmsListenerContainerFactory().apply {
                cfer.configure(this, cf)
            }

    @Bean
    open fun jacksonJmsMessageConverter() = MappingJackson2MessageConverter().apply {
        setTargetType(TEXT)
        setTypeIdPropertyName("_type")
    }
}

const val AUDIT_QUEUE = "AUDIT_QUEUE"

@Component
internal open class AuditListener(private val ctx: ApplicationContext) {
    //
    @Transactional
    @JmsListener(destination = AUDIT_QUEUE)
    open fun audit(record: AuditRecord) {
        Class.forName(record.auditor).kotlin.createInstance().run { // custom serializer
            ctx.autowireCapableBeanFactory.autowireBean(this)
            (this as Auditor).process(record)
        }
    }
}
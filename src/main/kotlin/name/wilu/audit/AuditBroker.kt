package name.wilu.audit

import org.springframework.boot.autoconfigure.jms.DefaultJmsListenerContainerFactoryConfigurer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.jms.annotation.EnableJms
import org.springframework.jms.config.DefaultJmsListenerContainerFactory
import org.springframework.jms.core.JmsTemplate
import org.springframework.jms.support.converter.MappingJackson2MessageConverter
import org.springframework.jms.support.converter.MessageType.TEXT
import javax.jms.ConnectionFactory

@EnableJms
@Configuration
internal class AuditBroker {

    @Bean
    fun auditJmsListener(cf: ConnectionFactory, cfer: DefaultJmsListenerContainerFactoryConfigurer) =
            DefaultJmsListenerContainerFactory().apply {
                cfer.configure(this, cf)
            }

    @Bean
    fun jacksonConverter() = MappingJackson2MessageConverter().apply {
        setTargetType(TEXT)
        setTypeIdPropertyName("_type")
    }

    @Bean
    fun jmsTemplate(cf: ConnectionFactory) = JmsTemplate(cf)

}
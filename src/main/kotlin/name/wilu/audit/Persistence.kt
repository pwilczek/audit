package name.wilu.audit

import org.h2.tools.Server
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType.H2
import org.springframework.jms.core.JmsTemplate
import org.springframework.orm.jpa.JpaTransactionManager
import org.springframework.orm.jpa.JpaVendorAdapter
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean
import org.springframework.orm.jpa.vendor.Database
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter
import javax.sql.DataSource

@Configuration
internal open class Database {

    @Bean
    open fun dataSource() = EmbeddedDatabaseBuilder().setName("db").setType(H2).build()

    @Bean
    open fun httpServer() = Server
            .createTcpServer("-tcp", "-tcpAllowOthers", "-tcpPort", "9997").start()

    @Bean
    open fun webServer() = Server // jdbc:h2:tcp://localhost:9997/mem:db
            .createWebServer("-web", "-webAllowOthers", "-webPort", "9998").start()
}

@Configuration
internal open class Jpa {

    @Bean
    open fun vendor() = HibernateJpaVendorAdapter().apply {
        setDatabase(Database.H2)
        setShowSql(true)
        setGenerateDdl(true)
    }

    @Bean
    open fun entityManagerFactory(dataSource: DataSource,
                                  vendor: JpaVendorAdapter,
                                  auditInterceptor: AuditInterceptor) =
            LocalContainerEntityManagerFactoryBean().apply {
                this.dataSource = dataSource
                jpaVendorAdapter = vendor
                jpaPropertyMap["hibernate.session_factory.interceptor"] = auditInterceptor
                setPackagesToScan("name.wilu")
            }

    @Bean
    open fun auditInterceptor(jmsTemplate: JmsTemplate) = AuditInterceptor(jmsTemplate)

    @Bean
    open fun transactionManager(factory: LocalContainerEntityManagerFactoryBean) =
            JpaTransactionManager().apply {
                entityManagerFactory = factory.`object`
            }
}
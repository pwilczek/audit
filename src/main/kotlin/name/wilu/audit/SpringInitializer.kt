package name.wilu.audit

import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.stereotype.Component
import kotlin.reflect.KClass
import kotlin.reflect.full.createInstance

@Component
internal class SpringInitializer : ApplicationContextAware {
    //
    override fun setApplicationContext(applicationContext: ApplicationContext) {
        ctx = applicationContext
    }
    //
    companion object {
        private lateinit var ctx: ApplicationContext
        fun <B : Auditor> initialize(clazz: KClass<B>) = clazz.createInstance().apply {
            ctx.autowireCapableBeanFactory.autowireBean(this)
        }
    }
}
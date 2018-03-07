package name.wilu.audit

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
open class AuditApplication

fun main(args: Array<String>) {
    runApplication<AuditApplication>(*args)
}

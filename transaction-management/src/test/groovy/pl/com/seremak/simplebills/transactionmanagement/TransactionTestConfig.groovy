package pl.com.seremak.simplebills.transactionmanagement

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.FilterType
import pl.com.seremak.simplebills.transactionmanagement.config.SpringSecurity

@TestConfiguration
@ComponentScan(
        basePackages = 'pl.com.seremak.simplebills.transactionmanagement',
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = [SpringSecurity.class]
        )
)
class TransactionTestConfig {
}

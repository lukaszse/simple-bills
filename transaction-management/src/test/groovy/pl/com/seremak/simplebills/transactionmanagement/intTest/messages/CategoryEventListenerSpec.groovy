package pl.com.seremak.simplebills.transactionmanagement.intTest.messages

import org.springframework.beans.factory.annotation.Autowired
import pl.com.seremak.simplebills.transactionmanagement.service.TransactionService

import static pl.com.seremak.simplebills.transactionmanagement.intTest.testUtilsAndConfig.CategoryEventListenerSpecData.prepareCategoryEventDto

class CategoryEventListenerSpec extends MessageListenerSpec {

    @Autowired
    TransactionService transactionService

    def "should receive and handle Category message" () {

        given: "prepare CategoryEvent  message"
        def categoryEvent = prepareCategoryEventDto()

        when: "publish test message"
        testRabbitPublisher.sendCategoryEventMessage(categoryEvent)

        then:
        _ * transactionService.handleCategoryDeletion(categoryEvent)
    }
}

package pl.com.seremak.simplebills.transactionmanagement.intTest.testUtilsAndConfig

import pl.com.seremak.simplebills.commons.dto.queue.ActionType
import pl.com.seremak.simplebills.commons.dto.queue.CategoryEventDto
import pl.com.seremak.simplebills.commons.model.Category

class CategoryEventListenerSpecData {


    static def WAGES = "wages"

    static def prepareCategoryEventDto() {
        new CategoryEventDto(
                username: "testUser",
                categoryName: "salary",
                actionType: ActionType.DELETION,
                transactionType: Category.TransactionType.INCOME,
                replacementCategoryName: WAGES
        )
    }
}

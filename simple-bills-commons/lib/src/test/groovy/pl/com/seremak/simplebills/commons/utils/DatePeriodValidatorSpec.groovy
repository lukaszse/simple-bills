package pl.com.seremak.simplebills.commons.utils


import pl.com.seremak.simplebills.commons.dto.http.standardQueryParam.BasicDatePeriod
import pl.com.seremak.simplebills.commons.validator.DatePeriodValidator
import spock.lang.Specification

import javax.validation.ConstraintValidatorContext
import java.time.LocalDate

class DatePeriodValidatorSpec extends Specification {

    DatePeriodValidator timePeriodValidator = new DatePeriodValidator();
    ConstraintValidatorContext constraintValidatorContext = Mock()

    def "should validate TimePeriod correctly"() {

        expect: 'should return correct validation result'
        timePeriodValidator.isValid(timeperiod, constraintValidatorContext) == expectedResult

        where:
        timeperiod                                                                   || expectedResult
        BasicDatePeriod.of(LocalDate.now().plusDays(2), LocalDate.now().plusDays(3)) || true
        BasicDatePeriod.of(LocalDate.now().plusDays(5), LocalDate.now().plusDays(3)) || false
        BasicDatePeriod.of(LocalDate.now().plusDays(3), LocalDate.now().plusDays(3)) || true
    }
}

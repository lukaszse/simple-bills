package pl.com.seremak.simplebills.commons.validator;

import org.springframework.lang.Nullable;
import pl.com.seremak.simplebills.commons.dto.http.standardQueryParam.DatePeriod;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class DatePeriodValidator implements ConstraintValidator<ValidateDatePeriod, DatePeriod> {

    @Override
    public void initialize(ValidateDatePeriod timePeriod) {
    }

    @Override
    public boolean isValid(@Nullable DatePeriod period, ConstraintValidatorContext constraintValidatorContext) {
        return period == null || period.getDateFrom() == null || period.getDateTo() == null ||
        period.getDateFrom().isBefore(period.getDateTo()) || period.getDateFrom().isEqual(period.getDateTo());
    }
}

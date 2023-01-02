package pl.com.seremak.simplebills.commons.validator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = DatePeriodValidator.class)
@Target(value = {ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidateDatePeriod {
    String message() default "Invalid time period. Please check if chosen dates are correct!";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

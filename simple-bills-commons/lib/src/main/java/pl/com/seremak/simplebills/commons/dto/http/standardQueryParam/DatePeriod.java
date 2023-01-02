package pl.com.seremak.simplebills.commons.dto.http.standardQueryParam;

import java.time.LocalDate;

public interface DatePeriod {
    LocalDate getDateFrom();
    LocalDate getDateTo();
}

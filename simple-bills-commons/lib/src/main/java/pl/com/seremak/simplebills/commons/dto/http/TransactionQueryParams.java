package pl.com.seremak.simplebills.commons.dto.http;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import pl.com.seremak.simplebills.commons.dto.http.standardQueryParam.DatePeriod;
import pl.com.seremak.simplebills.commons.dto.http.standardQueryParam.PageQuery;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransactionQueryParams implements DatePeriod, PageQuery {

    private String category;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dateFrom;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dateTo;

    private Integer pageSize = 10;
    private Integer pageNumber = 1;
    private SortDirection sortDirection = SortDirection.DESC;
    private String sortColumn = "transactionNumber";

    private String searchTerm;
}

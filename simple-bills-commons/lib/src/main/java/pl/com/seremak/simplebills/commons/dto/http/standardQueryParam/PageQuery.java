package pl.com.seremak.simplebills.commons.dto.http.standardQueryParam;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import pl.com.seremak.simplebills.commons.dto.http.SortDirection;


public interface PageQuery {

    Integer getPageSize();
    Integer getPageNumber();
    SortDirection getSortDirection();
}

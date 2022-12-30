package pl.com.seremak.simplebills.commons.dto.http.standardQueryParam;

import pl.com.seremak.simplebills.commons.dto.http.SortDirection;


public interface PageQuery {

    Integer getPageSize();
    Integer getPageNumber();
    SortDirection getSortDirection();
    String getSortColumn();
}

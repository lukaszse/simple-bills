package pl.com.seremak.simplebills.commons.dto.http.standardQueryParam;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import pl.com.seremak.simplebills.commons.dto.http.SortDirection;

@Data
@NoArgsConstructor
@AllArgsConstructor(staticName = "of")
public class PageQuery {

    private Integer pageSize = 10;
    private Integer pageNumber = 1;
    private SortDirection sortDirection = SortDirection.DESC;
}

package pl.com.seremak.simplebills.commons.model;


import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserActivity extends VersionedEntity {

    public enum Activity {
        LOGGING_IN, LOGGING_OUT
    }

    private String username;
    private Activity activity;
}

package pl.com.seremak.simplebills.commons.dto.http;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserActivityDto {

    @NotBlank(message = "Activity cannot be blank")
    private String activity;
}

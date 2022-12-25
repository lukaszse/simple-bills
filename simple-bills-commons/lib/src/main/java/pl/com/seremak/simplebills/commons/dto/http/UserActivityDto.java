package pl.com.seremak.simplebills.commons.dto.http;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class UserActivityDto {

    @NotBlank(message = "Activity cannot be blank")
    private String activity;
}

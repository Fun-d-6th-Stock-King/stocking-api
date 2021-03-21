package com.stocking.modules.account;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Data
public class SignUpForm {

    @NotBlank
    private Long id;

    @NotBlank
    private Long uuid;

    @NotBlank
    @Length(min = 1, max = 20)
    @Pattern(regexp = "^[ㄱ-ㅎ가-힣a-zA-Z0-9_-]{1,20}$")
    private String nickname;

    private String email;

    private boolean checkSns;
}

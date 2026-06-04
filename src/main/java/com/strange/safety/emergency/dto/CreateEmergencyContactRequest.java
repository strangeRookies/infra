package com.strange.safety.emergency.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CreateEmergencyContactRequest {

    @NotBlank(message = "이름은 필수입니다.")
    private String name;

    @NotBlank(message = "관계는 필수입니다.")
    private String relationship;

    @NotBlank(message = "전화번호는 필수입니다.")
    @Pattern(regexp = "^[0-9\\-+]{7,20}$", message = "전화번호 형식이 올바르지 않습니다.")
    private String phoneNumber;
}

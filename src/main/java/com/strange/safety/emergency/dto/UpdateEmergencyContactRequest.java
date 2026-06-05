package com.strange.safety.emergency.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UpdateEmergencyContactRequest {

    private String name;
    private String relationship;
    private String phoneNumber;
}

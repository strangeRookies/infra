package com.strange.safety.camera.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UpdateRoiConfigRequest {

    private String polygonPoints;
    private Boolean isActive;
}

package com.strange.safety.camera.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CreateRoiConfigRequest {

    @NotNull(message = "시나리오 ID는 필수입니다.")
    private Long scenarioId;

    @NotBlank(message = "다각형 좌표는 필수입니다.")
    private String polygonPoints;
}

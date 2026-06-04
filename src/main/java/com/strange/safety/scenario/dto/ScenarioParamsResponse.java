package com.strange.safety.scenario.dto;

import com.strange.safety.scenario.entity.ScenarioParams;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ScenarioParamsResponse {

    private Long scenarioParamId;
    private Long scenarioId;
    private Integer timeThresholdSec;
    private Float motionThreshold;
    private String timeRestriction;
    private LocalDateTime updatedAt;

    public static ScenarioParamsResponse from(ScenarioParams params) {
        return ScenarioParamsResponse.builder()
                .scenarioParamId(params.getId())
                .scenarioId(params.getScenario().getId())
                .timeThresholdSec(params.getTimeThresholdSec())
                .motionThreshold(params.getMotionThreshold())
                .timeRestriction(params.getTimeRestriction())
                .updatedAt(params.getUpdatedAt())
                .build();
    }
}

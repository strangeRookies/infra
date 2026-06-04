package com.strange.safety.scenario.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UpdateScenarioParamsRequest {

    private Integer timeThresholdSec;
    private Float motionThreshold;
    private String timeRestriction;
}

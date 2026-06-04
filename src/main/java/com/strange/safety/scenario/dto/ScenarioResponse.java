package com.strange.safety.scenario.dto;

import com.strange.safety.scenario.entity.Scenario;
import com.strange.safety.scenario.entity.ScenarioType;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ScenarioResponse {

    private Long scenarioId;
    private ScenarioType scenarioType;
    private String description;

    public static ScenarioResponse from(Scenario scenario) {
        return ScenarioResponse.builder()
                .scenarioId(scenario.getId())
                .scenarioType(scenario.getScenarioType())
                .description(scenario.getDescription())
                .build();
    }
}

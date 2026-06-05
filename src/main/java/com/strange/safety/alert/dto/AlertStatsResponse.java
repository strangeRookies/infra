package com.strange.safety.alert.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class AlertStatsResponse {

    private long total;
    private long warning;
    private long critical;
    private long pending;
    private long confirmed;
    private long dismissed;
    private List<ScenarioCount> byScenario;

    @Getter
    @Builder
    public static class ScenarioCount {
        private String scenarioType;
        private long count;
    }
}

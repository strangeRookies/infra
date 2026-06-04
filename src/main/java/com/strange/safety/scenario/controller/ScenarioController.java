package com.strange.safety.scenario.controller;

import com.strange.safety.common.response.ApiResponse;
import com.strange.safety.scenario.dto.ScenarioParamsResponse;
import com.strange.safety.scenario.dto.ScenarioResponse;
import com.strange.safety.scenario.dto.UpdateScenarioParamsRequest;
import com.strange.safety.scenario.service.ScenarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/scenarios")
@RequiredArgsConstructor
public class ScenarioController {

    private final ScenarioService scenarioService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ScenarioResponse>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success(scenarioService.getAll()));
    }

    @GetMapping("/{scenarioId}/params")
    public ResponseEntity<ApiResponse<ScenarioParamsResponse>> getParams(
            @PathVariable Long scenarioId) {
        return ResponseEntity.ok(ApiResponse.success(scenarioService.getParams(scenarioId)));
    }

    @PutMapping("/{scenarioId}/params")
    public ResponseEntity<ApiResponse<ScenarioParamsResponse>> updateParams(
            @PathVariable Long scenarioId,
            @RequestBody UpdateScenarioParamsRequest request) {
        return ResponseEntity.ok(ApiResponse.success(scenarioService.updateParams(scenarioId, request)));
    }
}

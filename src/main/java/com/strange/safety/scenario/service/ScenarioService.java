package com.strange.safety.scenario.service;

import com.strange.safety.common.exception.CustomException;
import com.strange.safety.common.exception.ErrorCode;
import com.strange.safety.scenario.dto.ScenarioParamsResponse;
import com.strange.safety.scenario.dto.ScenarioResponse;
import com.strange.safety.scenario.dto.UpdateScenarioParamsRequest;
import com.strange.safety.scenario.entity.ScenarioParams;
import com.strange.safety.scenario.repository.ScenarioParamsRepository;
import com.strange.safety.scenario.repository.ScenarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ScenarioService {

    private final ScenarioRepository scenarioRepository;
    private final ScenarioParamsRepository scenarioParamsRepository;

    public List<ScenarioResponse> getAll() {
        return scenarioRepository.findAll().stream()
                .map(ScenarioResponse::from)
                .collect(Collectors.toList());
    }

    public ScenarioParamsResponse getParams(Long scenarioId) {
        validateScenarioExists(scenarioId);
        ScenarioParams params = scenarioParamsRepository.findByScenario_Id(scenarioId)
                .orElseThrow(() -> new CustomException(ErrorCode.SCENARIO_PARAMS_NOT_FOUND));
        return ScenarioParamsResponse.from(params);
    }

    @Transactional
    public ScenarioParamsResponse updateParams(Long scenarioId, UpdateScenarioParamsRequest request) {
        validateScenarioExists(scenarioId);
        ScenarioParams params = scenarioParamsRepository.findByScenario_Id(scenarioId)
                .orElseThrow(() -> new CustomException(ErrorCode.SCENARIO_PARAMS_NOT_FOUND));
        params.update(request.getTimeThresholdSec(), request.getMotionThreshold(), request.getTimeRestriction());
        return ScenarioParamsResponse.from(params);
    }

    private void validateScenarioExists(Long scenarioId) {
        if (!scenarioRepository.existsById(scenarioId)) {
            throw new CustomException(ErrorCode.SCENARIO_NOT_FOUND);
        }
    }
}

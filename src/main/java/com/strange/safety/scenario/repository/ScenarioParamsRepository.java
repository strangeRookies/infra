package com.strange.safety.scenario.repository;

import com.strange.safety.scenario.entity.ScenarioParams;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ScenarioParamsRepository extends JpaRepository<ScenarioParams, Long> {
    Optional<ScenarioParams> findByScenario_Id(Long scenarioId);
}

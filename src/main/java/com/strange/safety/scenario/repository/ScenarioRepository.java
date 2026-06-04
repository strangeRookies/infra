package com.strange.safety.scenario.repository;

import com.strange.safety.scenario.entity.Scenario;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ScenarioRepository extends JpaRepository<Scenario, Long> {
}

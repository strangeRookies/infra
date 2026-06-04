package com.strange.safety.scenario.entity;

import com.strange.safety.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "scenarios")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Scenario extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "scenario_id")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "scenario_type", nullable = false, unique = true, length = 30)
    private ScenarioType scenarioType;

    @Column(nullable = false)
    private String description;

    @Builder
    private Scenario(ScenarioType scenarioType, String description) {
        this.scenarioType = scenarioType;
        this.description = description;
    }
}

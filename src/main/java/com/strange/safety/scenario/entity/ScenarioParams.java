package com.strange.safety.scenario.entity;

import com.strange.safety.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "scenario_params")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ScenarioParams extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "scenario_param_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "scenario_id", nullable = false, unique = true)
    private Scenario scenario;

    @Column(name = "time_threshold_sec", nullable = false)
    private Integer timeThresholdSec;

    @Column(name = "motion_threshold", nullable = false)
    private Float motionThreshold;

    @Column(name = "time_restriction")
    private String timeRestriction;

    @Builder
    private ScenarioParams(Scenario scenario, Integer timeThresholdSec,
                           Float motionThreshold, String timeRestriction) {
        this.scenario = scenario;
        this.timeThresholdSec = timeThresholdSec;
        this.motionThreshold = motionThreshold;
        this.timeRestriction = timeRestriction;
    }

    public void update(Integer timeThresholdSec, Float motionThreshold, String timeRestriction) {
        if (timeThresholdSec != null) this.timeThresholdSec = timeThresholdSec;
        if (motionThreshold != null) this.motionThreshold = motionThreshold;
        if (timeRestriction != null) this.timeRestriction = timeRestriction;
    }
}

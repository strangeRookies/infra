package com.strange.safety.camera.service;

import com.strange.safety.camera.dto.CreateRoiConfigRequest;
import com.strange.safety.camera.dto.RoiConfigResponse;
import com.strange.safety.camera.dto.UpdateRoiConfigRequest;
import com.strange.safety.camera.entity.Camera;
import com.strange.safety.camera.entity.RoiConfig;
import com.strange.safety.camera.repository.CameraRepository;
import com.strange.safety.camera.repository.RoiConfigRepository;
import com.strange.safety.common.exception.CustomException;
import com.strange.safety.common.exception.ErrorCode;
import com.strange.safety.facility.service.FacilityService;
import com.strange.safety.scenario.entity.Scenario;
import com.strange.safety.scenario.repository.ScenarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RoiConfigService {

    private final RoiConfigRepository roiConfigRepository;
    private final CameraRepository cameraRepository;
    private final ScenarioRepository scenarioRepository;
    private final FacilityService facilityService;

    @Transactional
    public RoiConfigResponse create(Long userId, Long cameraId, CreateRoiConfigRequest request) {
        Camera camera = getCameraWithOwnerCheck(userId, cameraId);
        Scenario scenario = scenarioRepository.findById(request.getScenarioId())
                .orElseThrow(() -> new CustomException(ErrorCode.SCENARIO_NOT_FOUND));

        RoiConfig roiConfig = RoiConfig.builder()
                .camera(camera)
                .scenario(scenario)
                .polygonPoints(request.getPolygonPoints())
                .build();

        return RoiConfigResponse.from(roiConfigRepository.save(roiConfig));
    }

    public List<RoiConfigResponse> getList(Long userId, Long cameraId) {
        getCameraWithOwnerCheck(userId, cameraId);
        return roiConfigRepository.findByCamera_Id(cameraId).stream()
                .map(RoiConfigResponse::from)
                .collect(Collectors.toList());
    }

    public RoiConfigResponse getOne(Long userId, Long roiConfigId) {
        RoiConfig roiConfig = getRoiWithOwnerCheck(userId, roiConfigId);
        return RoiConfigResponse.from(roiConfig);
    }

    @Transactional
    public RoiConfigResponse update(Long userId, Long roiConfigId, UpdateRoiConfigRequest request) {
        RoiConfig roiConfig = getRoiWithOwnerCheck(userId, roiConfigId);
        roiConfig.update(request.getPolygonPoints(), request.getIsActive());
        return RoiConfigResponse.from(roiConfig);
    }

    @Transactional
    public void delete(Long userId, Long roiConfigId) {
        RoiConfig roiConfig = getRoiWithOwnerCheck(userId, roiConfigId);
        roiConfig.deactivate();
    }

    private Camera getCameraWithOwnerCheck(Long userId, Long cameraId) {
        Camera camera = cameraRepository.findById(cameraId)
                .orElseThrow(() -> new CustomException(ErrorCode.CAMERA_NOT_FOUND));
        facilityService.getFacilityWithOwnerCheck(userId, camera.getFacility().getId());
        return camera;
    }

    private RoiConfig getRoiWithOwnerCheck(Long userId, Long roiConfigId) {
        RoiConfig roiConfig = roiConfigRepository.findById(roiConfigId)
                .orElseThrow(() -> new CustomException(ErrorCode.ROI_CONFIG_NOT_FOUND));
        facilityService.getFacilityWithOwnerCheck(userId, roiConfig.getCamera().getFacility().getId());
        return roiConfig;
    }
}

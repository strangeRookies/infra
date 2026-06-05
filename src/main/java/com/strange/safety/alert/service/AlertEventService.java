package com.strange.safety.alert.service;

import com.strange.safety.alert.dto.AlertEventDetailResponse;
import com.strange.safety.alert.dto.AlertEventResponse;
import com.strange.safety.alert.dto.AlertStatsResponse;
import com.strange.safety.alert.dto.SnapshotResponse;
import com.strange.safety.alert.entity.AlertEvent;
import com.strange.safety.alert.entity.AlertSeverity;
import com.strange.safety.alert.entity.AlertStatus;
import com.strange.safety.alert.repository.AlertEventRepository;
import com.strange.safety.alert.repository.SnapshotRepository;
import com.strange.safety.common.exception.CustomException;
import com.strange.safety.common.exception.ErrorCode;
import com.strange.safety.facility.service.FacilityService;
import com.strange.safety.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AlertEventService {

    private final AlertEventRepository alertEventRepository;
    private final SnapshotRepository snapshotRepository;
    private final FacilityService facilityService;
    private final UserRepository userRepository;

    public Page<AlertEventResponse> getList(Long userId, Long facilityId,
                                            AlertSeverity severity, AlertStatus status,
                                            LocalDateTime dateFrom, LocalDateTime dateTo,
                                            Pageable pageable) {
        facilityService.getFacilityWithOwnerCheck(userId, facilityId);

        Specification<AlertEvent> spec = Specification
                .where(facilityEquals(facilityId))
                .and(severity != null ? (r, q, cb) -> cb.equal(r.get("severity"), severity) : null)
                .and(status != null ? (r, q, cb) -> cb.equal(r.get("status"), status) : null)
                .and(dateFrom != null ? (r, q, cb) -> cb.greaterThanOrEqualTo(r.get("detectedAt"), dateFrom) : null)
                .and(dateTo != null ? (r, q, cb) -> cb.lessThanOrEqualTo(r.get("detectedAt"), dateTo) : null);

        return alertEventRepository.findAll(spec, pageable).map(AlertEventResponse::from);
    }

    public AlertEventDetailResponse getDetail(Long userId, Long alertEventId) {
        AlertEvent event = getEventWithOwnerCheck(userId, alertEventId);
        List<SnapshotResponse> snapshots = snapshotRepository.findByAlertEvent_Id(alertEventId)
                .stream().map(SnapshotResponse::from).collect(Collectors.toList());
        return AlertEventDetailResponse.from(event, snapshots);
    }

    @Transactional
    public AlertEventResponse acknowledge(Long userId, Long alertEventId) {
        AlertEvent event = getEventWithOwnerCheck(userId, alertEventId);
        event.acknowledge(userRepository.getReferenceById(userId));
        return AlertEventResponse.from(event);
    }

    public AlertStatsResponse getStats(Long userId, Long facilityId,
                                       LocalDateTime dateFrom, LocalDateTime dateTo) {
        facilityService.getFacilityWithOwnerCheck(userId, facilityId);

        // null 파라미터 바인딩 오류 방지 — 범위 미지정 시 전체 기간으로 대체
        LocalDateTime from = dateFrom != null ? dateFrom : LocalDateTime.of(2000, 1, 1, 0, 0);
        LocalDateTime to   = dateTo   != null ? dateTo   : LocalDateTime.now().plusYears(10);

        long total     = alertEventRepository.countByFacilityAndDateRange(facilityId, from, to);
        long warning   = alertEventRepository.countByFacilityAndSeverity(facilityId, AlertSeverity.WARNING, from, to);
        long critical  = alertEventRepository.countByFacilityAndSeverity(facilityId, AlertSeverity.CRITICAL, from, to);
        long pending   = alertEventRepository.countByFacilityAndStatus(facilityId, AlertStatus.PENDING, from, to);
        long confirmed = alertEventRepository.countByFacilityAndStatus(facilityId, AlertStatus.CONFIRMED, from, to);
        long dismissed = alertEventRepository.countByFacilityAndStatus(facilityId, AlertStatus.DISMISSED, from, to);

        List<AlertStatsResponse.ScenarioCount> byScenario =
                alertEventRepository.countGroupByScenario(facilityId, from, to).stream()
                        .map(row -> AlertStatsResponse.ScenarioCount.builder()
                                .scenarioType((String) row[0])
                                .count(((Number) row[1]).longValue())
                                .build())
                        .collect(Collectors.toList());

        return AlertStatsResponse.builder()
                .total(total).warning(warning).critical(critical)
                .pending(pending).confirmed(confirmed).dismissed(dismissed)
                .byScenario(byScenario)
                .build();
    }

    private AlertEvent getEventWithOwnerCheck(Long userId, Long alertEventId) {
        AlertEvent event = alertEventRepository.findById(alertEventId)
                .orElseThrow(() -> new CustomException(ErrorCode.ALERT_NOT_FOUND));
        facilityService.getFacilityWithOwnerCheck(userId, event.getCamera().getFacility().getId());
        return event;
    }

    private static Specification<AlertEvent> facilityEquals(Long facilityId) {
        return (root, query, cb) -> {
            query.distinct(true);
            return cb.equal(
                    root.join("camera").join("facility").get("id"),
                    facilityId);
        };
    }
}

package com.strange.safety.alert.dto;

import com.strange.safety.alert.entity.Snapshot;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class SnapshotResponse {

    private Long snapshotId;
    private String snapshotUrl;
    private Long fileSizeBytes;
    private LocalDateTime createdAt;

    public static SnapshotResponse from(Snapshot snapshot) {
        return SnapshotResponse.builder()
                .snapshotId(snapshot.getId())
                .snapshotUrl(snapshot.getSnapshotUrl())
                .fileSizeBytes(snapshot.getFileSizeBytes())
                .createdAt(snapshot.getCreatedAt())
                .build();
    }
}

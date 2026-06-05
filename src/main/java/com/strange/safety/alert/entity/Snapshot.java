package com.strange.safety.alert.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "snapshots")
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Snapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "snapshot_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "alert_event_id", nullable = false)
    private AlertEvent alertEvent;

    @Column(name = "snapshot_url", nullable = false)
    private String snapshotUrl;

    @Column(name = "file_size_bytes")
    private Long fileSizeBytes;

    @CreatedDate
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @Builder
    private Snapshot(AlertEvent alertEvent, String snapshotUrl, Long fileSizeBytes) {
        this.alertEvent = alertEvent;
        this.snapshotUrl = snapshotUrl;
        this.fileSizeBytes = fileSizeBytes;
    }
}

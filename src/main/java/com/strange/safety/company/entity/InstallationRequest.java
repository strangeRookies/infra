package com.strange.safety.company.entity;

import com.strange.safety.common.entity.BaseEntity;
import jakarta.persistence.*;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "installation_requests")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InstallationRequest extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "installation_request_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "company_profile_id", nullable = false)
    private CompanyProfile companyProfile;

    @Column(name = "installation_count", nullable = false)
    private String installationCount;

    @Column(name = "preferred_date", nullable = false)
    private LocalDate preferredDate;

    @Column(name = "special_request", length = 1000)
    private String specialRequest;

    @Builder
    private InstallationRequest(CompanyProfile companyProfile, String installationCount,
                                LocalDate preferredDate, String specialRequest) {
        this.companyProfile = companyProfile;
        this.installationCount = installationCount;
        this.preferredDate = preferredDate;
        this.specialRequest = specialRequest;
    }
}

package com.strange.safety.user.entity;

import com.strange.safety.common.entity.BaseEntity;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "user_agreements",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_user_agreements_user_type",
                columnNames = {"user_id", "agreement_type"}
        )
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserAgreement extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "agreement_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "agreement_type", nullable = false, length = 30)
    private AgreementType agreementType;

    @Column(name = "is_required", nullable = false)
    private boolean required;

    @Column(name = "is_agreed", nullable = false)
    private boolean agreed;

    @Column(name = "agreed_at")
    private LocalDateTime agreedAt;

    @Column(name = "withdrawn_at")
    private LocalDateTime withdrawnAt;

    @Builder
    private UserAgreement(User user, AgreementType agreementType, boolean required,
                          boolean agreed, LocalDateTime agreedAt, LocalDateTime withdrawnAt) {
        this.user = user;
        this.agreementType = agreementType;
        this.required = required;
        this.agreed = agreed;
        this.agreedAt = agreedAt;
        this.withdrawnAt = withdrawnAt;
    }

    public static UserAgreement create(User user, AgreementType agreementType,
                                       boolean required, boolean agreed, LocalDateTime now) {
        return UserAgreement.builder()
                .user(user)
                .agreementType(agreementType)
                .required(required)
                .agreed(agreed)
                .agreedAt(agreed ? now : null)
                .withdrawnAt(agreed ? null : now)
                .build();
    }

    public void updateAgreement(boolean agreed, LocalDateTime now) {
        this.agreed = agreed;
        if (agreed) {
            this.agreedAt = now;
            this.withdrawnAt = null;
            return;
        }
        this.withdrawnAt = now;
    }
}

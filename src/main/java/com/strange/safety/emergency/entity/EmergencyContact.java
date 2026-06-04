package com.strange.safety.emergency.entity;

import com.strange.safety.common.entity.BaseEntity;
import com.strange.safety.facility.entity.ProtectedTarget;
import com.strange.safety.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "emergency_contacts")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EmergencyContact extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "emergency_contact_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "guardian_user_id", nullable = false)
    private User guardianUser;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "protected_target_id", nullable = false)
    private ProtectedTarget protectedTarget;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 100)
    private String relationship;

    @Column(name = "phone_number", nullable = false, length = 30)
    private String phoneNumber;

    @Builder
    private EmergencyContact(User guardianUser, ProtectedTarget protectedTarget, String name,
                             String relationship, String phoneNumber) {
        this.guardianUser = guardianUser;
        this.protectedTarget = protectedTarget;
        this.name = name;
        this.relationship = relationship;
        this.phoneNumber = phoneNumber;
    }

    public void update(String name, String relationship, String phoneNumber) {
        if (name != null) this.name = name;
        if (relationship != null) this.relationship = relationship;
        if (phoneNumber != null) this.phoneNumber = phoneNumber;
    }
}

package com.strange.safety.inquiry.repository;

import com.strange.safety.inquiry.entity.Inquiry;
import com.strange.safety.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InquiryRepository extends JpaRepository<Inquiry, Long> {
    List<Inquiry> findAllByUserOrderByCreatedAtDesc(User user);
    List<Inquiry> findAllByOrderByCreatedAtDesc();
}

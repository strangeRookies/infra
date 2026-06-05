package com.strange.safety.inquiry.service;

import com.strange.safety.common.exception.CustomException;
import com.strange.safety.common.exception.ErrorCode;
import com.strange.safety.inquiry.dto.InquiryAnswerRequest;
import com.strange.safety.inquiry.dto.InquiryCreateRequest;
import com.strange.safety.inquiry.dto.InquiryResponse;
import com.strange.safety.inquiry.entity.Inquiry;
import com.strange.safety.inquiry.repository.InquiryRepository;
import com.strange.safety.user.entity.User;
import com.strange.safety.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InquiryService {

    private final InquiryRepository inquiryRepository;
    private final UserRepository userRepository;

    @Transactional
    public Long createInquiry(Long userId, InquiryCreateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        Inquiry inquiry = Inquiry.builder()
                .user(user)
                .category(request.getCategory())
                .title(request.getTitle())
                .content(request.getContent())
                .build();

        return inquiryRepository.save(inquiry).getId();
    }

    public List<InquiryResponse> getMyInquiries(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        return inquiryRepository.findAllByUserOrderByCreatedAtDesc(user).stream()
                .map(InquiryResponse::from)
                .collect(Collectors.toList());
    }

    public InquiryResponse getInquiry(Long inquiryId) {
        Inquiry inquiry = inquiryRepository.findById(inquiryId)
                .orElseThrow(() -> new CustomException(ErrorCode.INQUIRY_NOT_FOUND));
        return InquiryResponse.from(inquiry);
    }

    public List<InquiryResponse> getAllInquiries() {
        return inquiryRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(InquiryResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public void answerInquiry(Long inquiryId, Long adminId, InquiryAnswerRequest request) {
        Inquiry inquiry = inquiryRepository.findById(inquiryId)
                .orElseThrow(() -> new CustomException(ErrorCode.INQUIRY_NOT_FOUND));

        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        inquiry.addAnswer(request.getAnswer(), admin);
    }
}

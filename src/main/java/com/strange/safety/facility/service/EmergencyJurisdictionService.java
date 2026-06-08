package com.strange.safety.facility.service;

import com.strange.safety.common.exception.CustomException;
import com.strange.safety.common.exception.ErrorCode;
import com.strange.safety.facility.dto.EmergencyJurisdictionResolveRequest;
import com.strange.safety.facility.dto.EmergencyJurisdictionResponse;
import jakarta.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmergencyJurisdictionService {

    private static final String DATA_PATH = "data/emergency-jurisdictions.csv";

    private final List<EmergencyJurisdictionRecord> jurisdictions = new ArrayList<>();

    @PostConstruct
    void loadJurisdictions() throws IOException {
        ClassPathResource resource = new ClassPathResource(DATA_PATH);
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                resource.getInputStream(), StandardCharsets.UTF_8))) {
            reader.lines()
                    .skip(1)
                    .map(String::trim)
                    .filter(line -> !line.isBlank())
                    .map(this::parseLine)
                    .forEach(jurisdictions::add);
        }
    }

    public EmergencyJurisdictionResponse resolve(EmergencyJurisdictionResolveRequest request) {
        String address = normalize(request.address() + " " + nullToEmpty(request.addressDetail()));
        return jurisdictions.stream()
                .filter(record -> record.matches(address))
                .findFirst()
                .map(EmergencyJurisdictionRecord::toResponse)
                .orElseThrow(() -> new CustomException(ErrorCode.EMERGENCY_JURISDICTION_NOT_FOUND));
    }

    private EmergencyJurisdictionRecord parseLine(String line) {
        String[] values = line.split(",", -1);
        if (values.length != 8) {
            throw new IllegalStateException("Invalid emergency jurisdiction data: " + line);
        }
        return new EmergencyJurisdictionRecord(
                values[0].trim(),
                values[1].trim(),
                values[2].trim(),
                values[3].trim(),
                values[4].trim(),
                values[5].trim(),
                parseDouble(values[6]),
                parseDouble(values[7])
        );
    }

    private Double parseDouble(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return Double.valueOf(value.trim());
    }

    private String normalize(String value) {
        return value.replaceAll("\\s+", "").toLowerCase(Locale.ROOT);
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private record EmergencyJurisdictionRecord(
            String province,
            String district,
            String jurisdiction,
            String stationName,
            String centerName,
            String stationAddress,
            Double latitude,
            Double longitude
    ) {
        boolean matches(String normalizedAddress) {
            return matchesProvince(normalizedAddress) && normalizedAddress.contains(normalize(district));
        }

        EmergencyJurisdictionResponse toResponse() {
            return new EmergencyJurisdictionResponse(
                    district,
                    jurisdiction,
                    stationName,
                    centerName,
                    stationAddress,
                    latitude,
                    longitude
            );
        }

        private boolean matchesProvince(String normalizedAddress) {
            String normalizedProvince = normalize(province);
            if (normalizedAddress.contains(normalizedProvince)) {
                return true;
            }
            return switch (province) {
                case "서울특별시" -> normalizedAddress.contains("서울");
                case "부산광역시" -> normalizedAddress.contains("부산");
                case "대구광역시" -> normalizedAddress.contains("대구");
                case "인천광역시" -> normalizedAddress.contains("인천");
                case "광주광역시" -> normalizedAddress.contains("광주");
                case "대전광역시" -> normalizedAddress.contains("대전");
                case "울산광역시" -> normalizedAddress.contains("울산");
                case "세종특별자치시" -> normalizedAddress.contains("세종");
                default -> false;
            };
        }

        private String normalize(String value) {
            return value.replaceAll("\\s+", "").toLowerCase(Locale.ROOT);
        }
    }
}

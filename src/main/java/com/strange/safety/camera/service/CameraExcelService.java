package com.strange.safety.camera.service;

import com.strange.safety.camera.dto.BulkCameraUploadResult;
import com.strange.safety.camera.dto.CameraResponse;
import com.strange.safety.camera.entity.Camera;
import com.strange.safety.camera.repository.CameraRepository;
import com.strange.safety.common.exception.CustomException;
import com.strange.safety.common.exception.ErrorCode;
import com.strange.safety.common.util.AesUtil;
import com.strange.safety.facility.entity.Facility;
import com.strange.safety.facility.service.FacilityService;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CameraExcelService {

    private final CameraRepository cameraRepository;
    private final FacilityService facilityService;
    private final AesUtil aesUtil;

    private static final int HEADER_ROW_INDEX = 0;
    private static final int COL_CAMERA_NAME = 0;
    private static final int COL_SERIAL_NUMBER = 1;
    private static final int COL_RTSP_URL = 2;
    private static final int COL_LOCATION = 3;
    private static final int COL_LOGIN_ID = 4;
    private static final int COL_PASSWORD = 5;

    @Transactional
    public BulkCameraUploadResult bulkUpload(Long facilityId, MultipartFile file) {
        validateFileExtension(file);
        Facility facility = facilityService.getFacilityForAdmin(facilityId);

        List<Camera> camerasToSave = new ArrayList<>();
        List<BulkCameraUploadResult.FailedRow> failedRows = new ArrayList<>();

        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null) {
                throw new CustomException(ErrorCode.EXCEL_INVALID_FORMAT);
            }

            for (int rowIndex = HEADER_ROW_INDEX + 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row == null || isRowEmpty(row)) continue;

                try {
                    camerasToSave.add(rowToCamera(row, facility));
                } catch (IllegalArgumentException e) {
                    failedRows.add(new BulkCameraUploadResult.FailedRow(rowIndex + 1, e.getMessage()));
                }
            }
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            throw new CustomException(ErrorCode.EXCEL_PARSE_ERROR);
        }

        List<CameraResponse> registeredCameras = cameraRepository.saveAll(camerasToSave)
                .stream().map(CameraResponse::from).collect(Collectors.toList());

        return BulkCameraUploadResult.builder()
                .successCount(registeredCameras.size())
                .failCount(failedRows.size())
                .registeredCameras(registeredCameras)
                .failedRows(failedRows)
                .build();
    }

    private void validateFileExtension(MultipartFile file) {
        String filename = file.getOriginalFilename();
        if (filename == null || (!filename.endsWith(".xlsx") && !filename.endsWith(".xls"))) {
            throw new CustomException(ErrorCode.EXCEL_INVALID_FORMAT);
        }
    }

    private Camera rowToCamera(Row row, Facility facility) {
        String cameraName = getStringCellValue(row, COL_CAMERA_NAME);
        String serialNumber = getStringCellValue(row, COL_SERIAL_NUMBER);
        String rtspUrl = getStringCellValue(row, COL_RTSP_URL);
        String locationDescription = getStringCellValue(row, COL_LOCATION);
        String loginId = getStringCellValue(row, COL_LOGIN_ID);
        String password = getStringCellValue(row, COL_PASSWORD);

        if (cameraName == null || cameraName.isBlank()) {
            throw new IllegalArgumentException("카메라 이름 누락");
        }
        if (serialNumber == null || serialNumber.isBlank()) {
            throw new IllegalArgumentException("시리얼 번호 누락");
        }
        if (rtspUrl == null || rtspUrl.isBlank()) {
            throw new IllegalArgumentException("RTSP 주소 누락");
        }

        String encryptedPassword = (password != null && !password.isBlank())
                ? aesUtil.encrypt(password) : null;

        return Camera.builder()
                .facility(facility)
                .cameraName(cameraName)
                .cameraSerialNumber(serialNumber)
                .rtspUrl(rtspUrl)
                .locationDescription(locationDescription)
                .cameraLoginId(loginId)
                .cameraPasswordEncrypted(encryptedPassword)
                .build();
    }

    private boolean isRowEmpty(Row row) {
        for (int col = COL_CAMERA_NAME; col <= COL_PASSWORD; col++) {
            String value = getStringCellValue(row, col);
            if (value != null && !value.isBlank()) return false;
        }
        return true;
    }

    private String getStringCellValue(Row row, int colIndex) {
        Cell cell = row.getCell(colIndex);
        if (cell == null) return null;

        return switch (cell.getCellType()) {
            case STRING -> {
                String val = cell.getStringCellValue().trim();
                yield val.isEmpty() ? null : val;
            }
            case NUMERIC -> {
                if (DateUtil.isCellDateFormatted(cell)) yield null;
                double numValue = cell.getNumericCellValue();
                if (numValue == Math.floor(numValue) && !Double.isInfinite(numValue)) {
                    yield String.valueOf((long) numValue);
                }
                yield String.valueOf(numValue);
            }
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> {
                try {
                    String val = cell.getStringCellValue().trim();
                    yield val.isEmpty() ? null : val;
                } catch (Exception e) {
                    yield String.valueOf(cell.getNumericCellValue());
                }
            }
            default -> null;
        };
    }
}

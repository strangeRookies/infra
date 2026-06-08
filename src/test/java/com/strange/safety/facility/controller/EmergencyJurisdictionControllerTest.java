package com.strange.safety.facility.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.strange.safety.event.MqttSafetyEventSubscriber;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class EmergencyJurisdictionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MqttSafetyEventSubscriber mqttSafetyEventSubscriber;

    @Test
    void resolveReturnsJurisdictionWithoutAuthentication() throws Exception {
        mockMvc.perform(post("/api/emergency-jurisdictions/resolve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "postcode": "04123",
                                  "address": "서울특별시 마포구 월드컵로 1",
                                  "addressDetail": "101동 101호"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.district").value("마포구"))
                .andExpect(jsonPath("$.data.jurisdiction").value("마포소방서"))
                .andExpect(jsonPath("$.data.stationName").value("마포소방서"))
                .andExpect(jsonPath("$.data.centerName").value("성산119안전센터"));
    }

    @Test
    void resolveWorksWithoutAddressDetail() throws Exception {
        mockMvc.perform(post("/api/emergency-jurisdictions/resolve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "postcode": "04123",
                                  "address": "서울특별시 마포구 월드컵로 1"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.district").value("마포구"))
                .andExpect(jsonPath("$.data.jurisdiction").value("마포소방서"));
    }

    @Test
    void resolveReturnsNotFoundForUnknownAddress() throws Exception {
        mockMvc.perform(post("/api/emergency-jurisdictions/resolve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "postcode": "00000",
                                  "address": "알수없는시 알수없는구 알수없는로 1"
                                }
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("EMERGENCY_JURISDICTION_NOT_FOUND"));
    }

    @Test
    void resolveRejectsBlankAddress() throws Exception {
        mockMvc.perform(post("/api/emergency-jurisdictions/resolve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "postcode": "04123",
                                  "address": ""
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("COMMON_INVALID_INPUT"))
                .andExpect(jsonPath("$.error.fieldErrors.address").exists());
    }
}

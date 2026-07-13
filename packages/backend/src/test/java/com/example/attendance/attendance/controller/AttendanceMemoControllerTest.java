package com.example.attendance.attendance.controller;

import com.example.attendance.attendance.dto.AttendanceRecordResponse;
import com.example.attendance.attendance.service.AttendanceService;
import com.example.attendance.common.config.CorsConfig;
import com.example.attendance.common.config.SecurityConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
    controllers = AttendanceController.class,
    excludeFilters = @ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE,
        classes = {SecurityConfig.class, CorsConfig.class}
    )
)
@Import(AttendanceMemoControllerTest.TestSecurityConfig.class)
@ActiveProfiles("test")
@DisplayName("打刻メモ Controller")
class AttendanceMemoControllerTest {

    @org.springframework.boot.test.context.TestConfiguration
    static class TestSecurityConfig {
        @Bean
        public SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
            http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
            return http.build();
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AttendanceService attendanceService;

    private static final UUID EMPLOYEE_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

    @Nested
    @DisplayName("POST /api/attendance/clock-in（メモ付き）")
    class ClockInWithMemo {

        @Test
        @DisplayName("メモ付き出勤打刻が201を返しメモがレスポンスに含まれる")
        void clockIn_withMemo_returns201WithMemo() throws Exception {
            // Arrange
            var response = new AttendanceRecordResponse(
                    UUID.randomUUID(),
                    LocalDate.of(2025, 1, 15),
                    Instant.parse("2025-01-15T00:00:00Z"),
                    null,
                    "リモートワーク",
                    null,
                    false
            );
            when(attendanceService.clockIn(eq(EMPLOYEE_ID), eq("リモートワーク")))
                    .thenReturn(response);

            // Act & Assert
            mockMvc.perform(post("/api/attendance/clock-in")
                            .contentType(APPLICATION_JSON)
                            .content("""
                                {"employeeId":"%s","memo":"リモートワーク"}
                                """.formatted(EMPLOYEE_ID)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.clockInMemo").value("リモートワーク"));
        }

        @Test
        @DisplayName("メモなし出勤打刻も201を返す")
        void clockIn_withoutMemo_returns201() throws Exception {
            // Arrange
            var response = new AttendanceRecordResponse(
                    UUID.randomUUID(),
                    LocalDate.of(2025, 1, 15),
                    Instant.parse("2025-01-15T00:00:00Z"),
                    null,
                    null,
                    null,
                    false
            );
            when(attendanceService.clockIn(eq(EMPLOYEE_ID), any()))
                    .thenReturn(response);

            // Act & Assert
            mockMvc.perform(post("/api/attendance/clock-in")
                            .contentType(APPLICATION_JSON)
                            .content("""
                                {"employeeId":"%s"}
                                """.formatted(EMPLOYEE_ID)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.clockInMemo").doesNotExist());
        }

        @Test
        @DisplayName("101文字のメモは400エラーを返す")
        void clockIn_memoTooLong_returns400() throws Exception {
            // Arrange
            var longMemo = "あ".repeat(101);

            // Act & Assert
            mockMvc.perform(post("/api/attendance/clock-in")
                            .contentType(APPLICATION_JSON)
                            .content("""
                                {"employeeId":"%s","memo":"%s"}
                                """.formatted(EMPLOYEE_ID, longMemo)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("POST /api/attendance/clock-out（メモ付き）")
    class ClockOutWithMemo {

        @Test
        @DisplayName("メモ付き退勤打刻が200を返しメモがレスポンスに含まれる")
        void clockOut_withMemo_returns200WithMemo() throws Exception {
            // Arrange
            var response = new AttendanceRecordResponse(
                    UUID.randomUUID(),
                    LocalDate.of(2025, 1, 15),
                    Instant.parse("2025-01-14T23:00:00Z"),
                    Instant.parse("2025-01-15T08:00:00Z"),
                    null,
                    "定時退社",
                    false
            );
            when(attendanceService.clockOut(eq(EMPLOYEE_ID), eq("定時退社")))
                    .thenReturn(response);

            // Act & Assert
            mockMvc.perform(post("/api/attendance/clock-out")
                            .contentType(APPLICATION_JSON)
                            .content("""
                                {"employeeId":"%s","memo":"定時退社"}
                                """.formatted(EMPLOYEE_ID)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.clockOutMemo").value("定時退社"));
        }
    }

    @Nested
    @DisplayName("PUT /api/attendance/{id}/memo")
    class UpdateMemo {

        @Test
        @DisplayName("メモ編集が200を返し更新後のメモがレスポンスに含まれる")
        void updateMemo_validRequest_returns200() throws Exception {
            // Arrange
            var recordId = UUID.randomUUID();
            var response = new AttendanceRecordResponse(
                    recordId,
                    LocalDate.of(2025, 1, 15),
                    Instant.parse("2025-01-14T23:00:00Z"),
                    Instant.parse("2025-01-15T08:00:00Z"),
                    "在宅勤務",
                    "残業あり",
                    false
            );
            when(attendanceService.updateMemo(eq(recordId), any(), eq("在宅勤務"), eq("残業あり")))
                    .thenReturn(response);

            // Act & Assert
            mockMvc.perform(put("/api/attendance/{id}/memo", recordId)
                            .contentType(APPLICATION_JSON)
                            .content("""
                                {"clockInMemo":"在宅勤務","clockOutMemo":"残業あり"}
                                """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.clockInMemo").value("在宅勤務"))
                    .andExpect(jsonPath("$.clockOutMemo").value("残業あり"));
        }
    }
}

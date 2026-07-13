package com.example.attendance.correction.service;

import com.example.attendance.attendance.entity.AttendanceRecord;
import com.example.attendance.attendance.repository.AttendanceRecordRepository;
import com.example.attendance.correction.entity.AttendanceCorrection;
import com.example.attendance.correction.entity.CorrectionStatus;
import com.example.attendance.correction.repository.AttendanceCorrectionRepository;
import com.example.attendance.department.entity.Department;
import com.example.attendance.employee.entity.Employee;
import com.example.attendance.employee.entity.Role;
import com.example.attendance.employee.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("修正申請承認時のメモ削除")
class CorrectionMemoServiceTest {

    @Mock
    private AttendanceCorrectionRepository correctionRepository;

    @Mock
    private AttendanceRecordRepository attendanceRecordRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    private CorrectionServiceImpl service;

    private Employee employee;
    private Employee manager;

    @BeforeEach
    void setUp() {
        service = new CorrectionServiceImpl(
                correctionRepository, attendanceRecordRepository, employeeRepository);

        var department = Department.builder()
                .id(UUID.randomUUID())
                .name("Engineering")
                .build();

        employee = Employee.builder()
                .id(UUID.randomUUID())
                .name("田中太郎")
                .email("tanaka@example.com")
                .password("hashed")
                .department(department)
                .role(Role.EMPLOYEE)
                .isManager(false)
                .hireDate(LocalDate.of(2024, 4, 1))
                .build();

        manager = Employee.builder()
                .id(UUID.randomUUID())
                .name("佐藤次郎")
                .email("sato@example.com")
                .password("hashed")
                .department(department)
                .role(Role.EMPLOYEE)
                .isManager(true)
                .hireDate(LocalDate.of(2020, 4, 1))
                .build();
    }

    @Test
    @DisplayName("修正申請が承認されると出勤メモ・退勤メモが削除される")
    void approve_existingRecord_clearsMemo() {
        // Arrange
        var record = AttendanceRecord.builder()
                .id(UUID.randomUUID())
                .employee(employee)
                .workDate(LocalDate.of(2025, 1, 15))
                .clockIn(Instant.parse("2025-01-14T23:00:00Z"))
                .clockOut(Instant.parse("2025-01-15T08:00:00Z"))
                .clockInMemo("リモートワーク")
                .clockOutMemo("定時退社")
                .corrected(false)
                .build();
        var correction = AttendanceCorrection.builder()
                .id(UUID.randomUUID())
                .attendanceRecord(record)
                .requester(employee)
                .targetDate(LocalDate.of(2025, 1, 15))
                .correctedClockIn(Instant.parse("2025-01-14T23:30:00Z"))
                .correctedClockOut(Instant.parse("2025-01-15T09:00:00Z"))
                .reason("打刻時刻を間違えました")
                .status(CorrectionStatus.PENDING)
                .version(0L)
                .build();

        when(correctionRepository.findById(correction.getId()))
                .thenReturn(Optional.of(correction));
        when(employeeRepository.findById(manager.getId()))
                .thenReturn(Optional.of(manager));
        when(correctionRepository.save(any(AttendanceCorrection.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(attendanceRecordRepository.save(any(AttendanceRecord.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        service.approve(correction.getId(), manager.getId(), 0L);

        // Assert
        assertThat(record.getClockInMemo()).isNull();
        assertThat(record.getClockOutMemo()).isNull();
        assertThat(record.isCorrected()).isTrue();
    }
}

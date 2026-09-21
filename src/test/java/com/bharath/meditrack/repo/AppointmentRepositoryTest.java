package com.bharath.meditrack.repo;

import com.bharath.meditrack.model.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppointmentRepositoryTest {

    @Mock
    private AppointmentRepository appointmentRepository;

    private Patient patient() {
        return Patient.builder().id(1L).firstName("John").lastName("Doe").email("john@test.com").build();
    }

    private Doctor doctor() {
        return Doctor.builder()
                .id(1L).name("Dr. Asha Rao").licenseNo("LIC-001")
                .consultationFee(new BigDecimal("600.00")).dailySlotCapacity(8).active(true)
                .build();
    }

    private Appointment appointment() {
        return Appointment.builder()
                .appointmentId(1L).appointmentNo("APT-12345678")
                .patient(patient()).doctor(doctor())
                .status(AppointmentStatus.REQUESTED)
                .scheduledDate(LocalDate.of(2026, 9, 25))
                .totalAmount(new BigDecimal("600.00"))
                .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void testFindAllReturnsAppointments() {
        when(appointmentRepository.findAll()).thenReturn(java.util.List.of(appointment()));

        var result = appointmentRepository.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getAppointmentNo()).isEqualTo("APT-12345678");
    }

    @Test
    void testCountByDoctorAndScheduledDateReturnsCorrectCount() {
        when(appointmentRepository.countByDoctorAndScheduledDate(any(Doctor.class), eq(LocalDate.of(2026, 9, 25))))
                .thenReturn(3L);

        long count = appointmentRepository.countByDoctorAndScheduledDate(doctor(), LocalDate.of(2026, 9, 25));

        assertThat(count).isEqualTo(3L);
    }

    @Test
    void testFindByIdReturnsAppointment() {
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment()));

        var result = appointmentRepository.findById(1L);

        assertThat(result).isPresent();
        assertThat(result.get().getAppointmentNo()).isEqualTo("APT-12345678");
    }

    @Test
    void testFindByIdReturnsEmptyWhenNotFound() {
        when(appointmentRepository.findById(99L)).thenReturn(Optional.empty());

        var result = appointmentRepository.findById(99L);

        assertThat(result).isEmpty();
    }

    @Test
    void testSaveReturnsAppointmentWithId() {
        Appointment toSave = Appointment.builder()
                .appointmentNo("APT-NEWAPPT").patient(patient()).doctor(doctor())
                .status(AppointmentStatus.REQUESTED)
                .scheduledDate(LocalDate.of(2026, 9, 26))
                .totalAmount(new BigDecimal("600.00"))
                .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now())
                .build();
        when(appointmentRepository.save(any(Appointment.class))).thenAnswer(inv -> {
            Appointment a = inv.getArgument(0);
            a.setAppointmentId(99L);
            return a;
        });

        Appointment saved = appointmentRepository.save(toSave);

        assertThat(saved.getAppointmentId()).isEqualTo(99L);
        assertThat(saved.getAppointmentNo()).isEqualTo("APT-NEWAPPT");
    }
}

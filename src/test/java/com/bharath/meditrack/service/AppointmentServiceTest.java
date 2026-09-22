package com.bharath.meditrack.service;

import com.bharath.meditrack.dto.AppointmentResponse;
import com.bharath.meditrack.dto.BookAppointmentRequest;
import com.bharath.meditrack.dto.CreateFeedbackRequest;
import com.bharath.meditrack.dto.FeedbackResponse;
import com.bharath.meditrack.exception.BusinessRuleException;
import com.bharath.meditrack.exception.DuplicateResourceException;
import com.bharath.meditrack.exception.ResourceNotFoundException;
import com.bharath.meditrack.model.*;
import com.bharath.meditrack.repo.AppointmentRepository;
import com.bharath.meditrack.repo.DoctorRepository;
import com.bharath.meditrack.repo.FeedbackRepository;
import com.bharath.meditrack.repo.PatientRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceTest {

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private DoctorRepository doctorRepository;

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private FeedbackRepository feedbackRepository;

    @InjectMocks
    private AppointmentService appointmentService;

    private Patient patient() {
        return Patient.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .build();
    }

    private Doctor doctor() {
        return Doctor.builder()
                .id(1L)
                .name("Dr. Asha Rao")
                .licenseNo("LIC-CARD-001")
                .consultationFee(new BigDecimal("600.00"))
                .dailySlotCapacity(8)
                .active(true)
                .build();
    }

    private Appointment appointment(AppointmentStatus status) {
        return Appointment.builder()
                .appointmentId(1L)
                .appointmentNo("APT-12345678")
                .patient(patient())
                .doctor(doctor())
                .status(status)
                .scheduledDate(LocalDate.of(2026, 9, 25))
                .totalAmount(new BigDecimal("600.00"))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void testFindAllReturnsAllAppointments() {
        when(appointmentRepository.findAll()).thenReturn(List.of(appointment(AppointmentStatus.REQUESTED)));

        List<AppointmentResponse> result = appointmentService.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo("REQUESTED");
        assertThat(result.get(0).getPatientName()).isEqualTo("John Doe");
        assertThat(result.get(0).getDoctorName()).isEqualTo("Dr. Asha Rao");
    }

    @Test
    void testFindByIdReturnsAppointment() {
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment(AppointmentStatus.REQUESTED)));

        AppointmentResponse result = appointmentService.findById(1L);

        assertThat(result.getAppointmentId()).isEqualTo(1L);
        assertThat(result.getStatus()).isEqualTo("REQUESTED");
    }

    @Test
    void testFindByIdThrowsWhenNotFound() {
        when(appointmentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> appointmentService.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Appointment with id 99 not found");
    }

    @Test
    void testBookAppointmentSuccess() {
        BookAppointmentRequest request = BookAppointmentRequest.builder()
                .patientId(1L)
                .doctorId(1L)
                .date(LocalDate.of(2026, 9, 25))
                .build();
        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient()));
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor()));
        when(appointmentRepository.countByDoctorAndScheduledDate(any(Doctor.class), eq(LocalDate.of(2026, 9, 25)))).thenReturn(0L);
        when(appointmentRepository.save(any(Appointment.class))).thenAnswer(inv -> {
            Appointment a = inv.getArgument(0);
            a.setAppointmentId(1L);
            return a;
        });

        AppointmentResponse result = appointmentService.book(request);

        assertThat(result.getStatus()).isEqualTo("REQUESTED");
        assertThat(result.getTotalAmount()).isEqualByComparingTo(new BigDecimal("600.00"));
        assertThat(result.getPatientId()).isEqualTo(1L);
        assertThat(result.getDoctorId()).isEqualTo(1L);
    }

    @Test
    void testBookAppointmentThrowsWhenPatientNotFound() {
        BookAppointmentRequest request = BookAppointmentRequest.builder()
                .patientId(99L)
                .doctorId(1L)
                .date(LocalDate.of(2026, 9, 25))
                .build();
        when(patientRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> appointmentService.book(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Patient with id 99 not found");
    }

    @Test
    void testBookAppointmentThrowsWhenDoctorNotFound() {
        BookAppointmentRequest request = BookAppointmentRequest.builder()
                .patientId(1L)
                .doctorId(99L)
                .date(LocalDate.of(2026, 9, 25))
                .build();
        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient()));
        when(doctorRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> appointmentService.book(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Doctor with id 99 not found");
    }

    @Test
    void testBookAppointmentThrowsWhenNoSlotsAvailable() {
        BookAppointmentRequest request = BookAppointmentRequest.builder()
                .patientId(1L)
                .doctorId(1L)
                .date(LocalDate.of(2026, 9, 25))
                .build();
        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient()));
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor()));
        when(appointmentRepository.countByDoctorAndScheduledDate(any(Doctor.class), eq(LocalDate.of(2026, 9, 25)))).thenReturn(8L);

        assertThatThrownBy(() -> appointmentService.book(request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("No slots available");
    }

    @Test
    void testCancelAppointmentSuccess() {
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment(AppointmentStatus.REQUESTED)));
        when(appointmentRepository.save(any(Appointment.class))).thenAnswer(inv -> inv.getArgument(0));

        AppointmentResponse result = appointmentService.cancel(1L);

        assertThat(result.getStatus()).isEqualTo("CANCELLED");
    }

    @Test
    void testCancelAppointmentThrowsWhenAlreadyCancelled() {
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment(AppointmentStatus.CANCELLED)));

        assertThatThrownBy(() -> appointmentService.cancel(1L))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("cannot be cancelled in status CANCELLED");
    }

    @Test
    void testCancelAppointmentThrowsWhenAlreadyCompleted() {
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment(AppointmentStatus.COMPLETED)));

        assertThatThrownBy(() -> appointmentService.cancel(1L))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("cannot be cancelled in status COMPLETED");
    }

    @Test
    void testCancelAppointmentThrowsWhenNotFound() {
        when(appointmentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> appointmentService.cancel(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Appointment with id 99 not found");
    }

    @Test
    void testSubmitFeedbackSuccess() {
        CreateFeedbackRequest request = CreateFeedbackRequest.builder()
                .rating(5)
                .comment("Great doctor!")
                .build();
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment(AppointmentStatus.COMPLETED)));
        when(feedbackRepository.existsByAppointmentId(1L)).thenReturn(false);
        when(feedbackRepository.save(any(Feedback.class))).thenAnswer(inv -> {
            Feedback f = inv.getArgument(0);
            f.setId(1L);
            return f;
        });

        FeedbackResponse result = appointmentService.submitFeedback(1L, request);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getRating()).isEqualTo(5);
        assertThat(result.getComment()).isEqualTo("Great doctor!");
    }

    @Test
    void testSubmitFeedbackThrowsWhenAppointmentNotFound() {
        CreateFeedbackRequest request = CreateFeedbackRequest.builder().rating(5).build();
        when(appointmentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> appointmentService.submitFeedback(99L, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Appointment with id 99 not found");
    }

    @Test
    void testSubmitFeedbackThrowsWhenNotCompleted() {
        CreateFeedbackRequest request = CreateFeedbackRequest.builder().rating(5).build();
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment(AppointmentStatus.REQUESTED)));

        assertThatThrownBy(() -> appointmentService.submitFeedback(1L, request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("Appointment must be completed to submit feedback");
    }

    @Test
    void testSubmitFeedbackThrowsWhenAlreadySubmitted() {
        CreateFeedbackRequest request = CreateFeedbackRequest.builder().rating(5).build();
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment(AppointmentStatus.COMPLETED)));
        when(feedbackRepository.existsByAppointmentId(1L)).thenReturn(true);

        assertThatThrownBy(() -> appointmentService.submitFeedback(1L, request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessage("Feedback already submitted for appointment 1");
    }
}

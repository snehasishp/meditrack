package com.bharath.meditrack.service;

import com.bharath.meditrack.dto.AppointmentResponse;
import com.bharath.meditrack.dto.BookAppointmentRequest;
import com.bharath.meditrack.dto.CreateFeedbackRequest;
import com.bharath.meditrack.dto.FeedbackResponse;
import com.bharath.meditrack.exception.BusinessRuleException;
import com.bharath.meditrack.exception.DuplicateResourceException;
import com.bharath.meditrack.exception.ResourceNotFoundException;
import com.bharath.meditrack.model.Appointment;
import com.bharath.meditrack.model.AppointmentStatus;
import com.bharath.meditrack.model.Doctor;
import com.bharath.meditrack.model.Feedback;
import com.bharath.meditrack.model.Patient;
import com.bharath.meditrack.repo.AppointmentRepository;
import com.bharath.meditrack.repo.DoctorRepository;
import com.bharath.meditrack.repo.FeedbackRepository;
import com.bharath.meditrack.repo.PatientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final FeedbackRepository feedbackRepository;

    public List<AppointmentResponse> findAll() {
        return appointmentRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    public AppointmentResponse findById(Long id) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment with id " + id + " not found"));
        return toResponse(appointment);
    }

    public AppointmentResponse book(BookAppointmentRequest request) {
        Patient patient = patientRepository.findById(request.getPatientId())
                .orElseThrow(() -> new ResourceNotFoundException("Patient with id " + request.getPatientId() + " not found"));
        Doctor doctor = doctorRepository.findById(request.getDoctorId())
                .orElseThrow(() -> new ResourceNotFoundException("Doctor with id " + request.getDoctorId() + " not found"));

        long booked = appointmentRepository.countByDoctorAndScheduledDate(doctor, request.getDate());
        if (booked >= doctor.getDailySlotCapacity()) {
            throw new BusinessRuleException("No slots available for doctor " + doctor.getName() + " on " + request.getDate());
        }

        Appointment appointment = Appointment.builder()
                .appointmentNo("APT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .patient(patient)
                .doctor(doctor)
                .status(AppointmentStatus.REQUESTED)
                .scheduledDate(request.getDate())
                .totalAmount(doctor.getConsultationFee())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        return toResponse(appointmentRepository.save(appointment));
    }

    public AppointmentResponse cancel(Long id) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment with id " + id + " not found"));

        if (appointment.getStatus() == AppointmentStatus.CANCELLED ||
            appointment.getStatus() == AppointmentStatus.COMPLETED) {
            throw new BusinessRuleException("Appointment cannot be cancelled in status " + appointment.getStatus());
        }

        appointment.setStatus(AppointmentStatus.CANCELLED);
        appointment.setUpdatedAt(LocalDateTime.now());
        return toResponse(appointmentRepository.save(appointment));
    }

    public FeedbackResponse submitFeedback(Long appointmentId, CreateFeedbackRequest request) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment with id " + appointmentId + " not found"));

        if (appointment.getStatus() != AppointmentStatus.COMPLETED) {
            throw new BusinessRuleException("Appointment must be completed to submit feedback");
        }

        if (feedbackRepository.existsByAppointmentId(appointmentId)) {
            throw new DuplicateResourceException("Feedback already submitted for appointment " + appointmentId);
        }

        Feedback feedback = Feedback.builder()
                .appointment(appointment)
                .rating(request.getRating())
                .comment(request.getComment())
                .createdAt(LocalDateTime.now())
                .build();
        return toFeedbackResponse(feedbackRepository.save(feedback));
    }

    private FeedbackResponse toFeedbackResponse(Feedback feedback) {
        return FeedbackResponse.builder()
                .id(feedback.getId())
                .appointmentId(feedback.getAppointment().getAppointmentId())
                .rating(feedback.getRating())
                .comment(feedback.getComment())
                .createdAt(feedback.getCreatedAt())
                .build();
    }

    private AppointmentResponse toResponse(Appointment appointment) {
        return AppointmentResponse.builder()
                .appointmentId(appointment.getAppointmentId())
                .appointmentNo(appointment.getAppointmentNo())
                .patientId(appointment.getPatient().getId())
                .patientName(appointment.getPatient().getFirstName() + " " + appointment.getPatient().getLastName())
                .doctorId(appointment.getDoctor().getId())
                .doctorName(appointment.getDoctor().getName())
                .status(appointment.getStatus().name())
                .scheduledDate(appointment.getScheduledDate())
                .totalAmount(appointment.getTotalAmount())
                .notes(appointment.getNotes())
                .createdAt(appointment.getCreatedAt())
                .updatedAt(appointment.getUpdatedAt())
                .build();
    }
}

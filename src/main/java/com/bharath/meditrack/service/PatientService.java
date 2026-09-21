package com.bharath.meditrack.service;

import com.bharath.meditrack.dto.CreatePatientRequest;
import com.bharath.meditrack.dto.PatientResponse;
import com.bharath.meditrack.exception.ResourceNotFoundException;
import com.bharath.meditrack.exception.ValidationException;
import com.bharath.meditrack.model.Patient;
import com.bharath.meditrack.repo.PatientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PatientService {

    private final PatientRepository patientRepository;

    public List<PatientResponse> findAll() {
        return patientRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    public PatientResponse findById(Long id) {
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Patient with id " + id + " not found"));
        return toResponse(patient);
    }

    public PatientResponse create(CreatePatientRequest request) {
        if (patientRepository.existsByEmail(request.getEmail())) {
            throw new ValidationException("Patient with email " + request.getEmail() + " already exists");
        }
        Patient patient = Patient.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .dateOfBirth(request.getDateOfBirth() != null ? LocalDate.parse(request.getDateOfBirth()) : null)
                .createdAt(LocalDateTime.now())
                .build();
        return toResponse(patientRepository.save(patient));
    }

    private PatientResponse toResponse(Patient patient) {
        return PatientResponse.builder()
                .id(patient.getId())
                .firstName(patient.getFirstName())
                .lastName(patient.getLastName())
                .email(patient.getEmail())
                .phone(patient.getPhone())
                .dateOfBirth(patient.getDateOfBirth())
                .build();
    }
}

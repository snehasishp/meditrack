package com.bharath.meditrack.service;

import com.bharath.meditrack.dto.CreatePatientRequest;
import com.bharath.meditrack.dto.PatientResponse;
import com.bharath.meditrack.exception.ResourceNotFoundException;
import com.bharath.meditrack.exception.ValidationException;
import com.bharath.meditrack.model.Patient;
import com.bharath.meditrack.repo.PatientRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PatientServiceTest {

    @Mock
    private PatientRepository patientRepository;

    @InjectMocks
    private PatientService patientService;

    @Test
    void testFindAllReturnsAllPatients() {
        Patient patient = Patient.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .phone("+1234567890")
                .dateOfBirth(LocalDate.of(1990, 5, 14))
                .build();
        when(patientRepository.findAll()).thenReturn(List.of(patient));

        List<PatientResponse> result = patientService.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(1L);
        assertThat(result.get(0).getFirstName()).isEqualTo("John");
        assertThat(result.get(0).getEmail()).isEqualTo("john@example.com");
    }

    @Test
    void testFindByIdReturnsPatient() {
        Patient patient = Patient.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .build();
        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));

        PatientResponse result = patientService.findById(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getFirstName()).isEqualTo("John");
    }

    @Test
    void testFindByIdThrowsWhenNotFound() {
        when(patientRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> patientService.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Patient with id 99 not found");
    }

    @Test
    void testCreatePatientSuccess() {
        CreatePatientRequest request = CreatePatientRequest.builder()
                .firstName("Jane")
                .lastName("Smith")
                .email("jane@example.com")
                .phone("+1987654321")
                .build();
        Patient saved = Patient.builder()
                .id(2L)
                .firstName("Jane")
                .lastName("Smith")
                .email("jane@example.com")
                .phone("+1987654321")
                .createdAt(java.time.LocalDateTime.now())
                .build();
        when(patientRepository.existsByEmail("jane@example.com")).thenReturn(false);
        when(patientRepository.save(org.mockito.ArgumentMatchers.any(Patient.class))).thenReturn(saved);

        PatientResponse result = patientService.create(request);

        assertThat(result.getId()).isEqualTo(2L);
        assertThat(result.getFirstName()).isEqualTo("Jane");
        assertThat(result.getEmail()).isEqualTo("jane@example.com");
    }

    @Test
    void testCreatePatientThrowsWhenEmailExists() {
        CreatePatientRequest request = CreatePatientRequest.builder()
                .firstName("Jane")
                .lastName("Smith")
                .email("existing@example.com")
                .build();
        when(patientRepository.existsByEmail("existing@example.com")).thenReturn(true);

        assertThatThrownBy(() -> patientService.create(request))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("already exists");
    }
}

package com.bharath.meditrack.repo;

import com.bharath.meditrack.model.Patient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PatientRepositoryTest {

    @Mock
    private PatientRepository patientRepository;

    private Patient patient(Long id, String email) {
        return Patient.builder().id(id).firstName("John").lastName("Doe").email(email).build();
    }

    @Test
    void testFindAllReturnsPatients() {
        when(patientRepository.findAll()).thenReturn(List.of(patient(1L, "john@test.com"), patient(2L, "jane@test.com")));

        List<Patient> result = patientRepository.findAll();

        assertThat(result).hasSize(2);
    }

    @Test
    void testExistsByEmailReturnsTrueWhenExists() {
        when(patientRepository.existsByEmail("john@test.com")).thenReturn(true);

        boolean exists = patientRepository.existsByEmail("john@test.com");

        assertThat(exists).isTrue();
    }

    @Test
    void testExistsByEmailReturnsFalseWhenNotExists() {
        when(patientRepository.existsByEmail("notfound@test.com")).thenReturn(false);

        boolean exists = patientRepository.existsByEmail("notfound@test.com");

        assertThat(exists).isFalse();
    }

    @Test
    void testFindByIdReturnsPatient() {
        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient(1L, "john@test.com")));

        Optional<Patient> result = patientRepository.findById(1L);

        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo("john@test.com");
    }

    @Test
    void testFindByIdReturnsEmptyWhenNotFound() {
        when(patientRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<Patient> result = patientRepository.findById(99L);

        assertThat(result).isEmpty();
    }
}

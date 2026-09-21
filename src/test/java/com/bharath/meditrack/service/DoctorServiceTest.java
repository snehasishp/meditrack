package com.bharath.meditrack.service;

import com.bharath.meditrack.dto.CreateDoctorRequest;
import com.bharath.meditrack.dto.DoctorResponse;
import com.bharath.meditrack.exception.ResourceNotFoundException;
import com.bharath.meditrack.model.Doctor;
import com.bharath.meditrack.model.Specialty;
import com.bharath.meditrack.repo.DoctorRepository;
import com.bharath.meditrack.repo.SpecialtyRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DoctorServiceTest {

    @Mock
    private DoctorRepository doctorRepository;

    @Mock
    private SpecialtyRepository specialtyRepository;

    @InjectMocks
    private DoctorService doctorService;

    @Test
    void testFindAllReturnsAllDoctors() {
        Specialty specialty = Specialty.builder()
                .id(1L)
                .name("Cardiology")
                .build();
        Doctor doctor = Doctor.builder()
                .id(1L)
                .name("Dr. Asha Rao")
                .licenseNo("LIC-CARD-001")
                .consultationFee(new BigDecimal("600.00"))
                .dailySlotCapacity(8)
                .active(true)
                .specialty(specialty)
                .createdAt(LocalDateTime.now())
                .build();
        when(doctorRepository.findAll()).thenReturn(List.of(doctor));

        List<DoctorResponse> result = doctorService.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Dr. Asha Rao");
        assertThat(result.get(0).getSpecialtyName()).isEqualTo("Cardiology");
    }

    @Test
    void testFindByIdReturnsDoctor() {
        Specialty specialty = Specialty.builder().id(1L).name("Cardiology").build();
        Doctor doctor = Doctor.builder()
                .id(1L)
                .name("Dr. Asha Rao")
                .licenseNo("LIC-CARD-001")
                .consultationFee(new BigDecimal("600.00"))
                .dailySlotCapacity(8)
                .active(true)
                .specialty(specialty)
                .build();
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));

        DoctorResponse result = doctorService.findById(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Dr. Asha Rao");
    }

    @Test
    void testFindByIdThrowsWhenNotFound() {
        when(doctorRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> doctorService.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Doctor with id 99 not found");
    }

    @Test
    void testCreateDoctorSuccess() {
        Specialty specialty = Specialty.builder().id(1L).name("Cardiology").build();
        CreateDoctorRequest request = CreateDoctorRequest.builder()
                .name("Dr. New")
                .licenseNo("LIC-NEW-001")
                .consultationFee(new BigDecimal("500.00"))
                .dailySlotCapacity(6)
                .active(true)
                .specialtyId(1L)
                .build();
        Doctor saved = Doctor.builder()
                .id(3L)
                .name("Dr. New")
                .licenseNo("LIC-NEW-001")
                .consultationFee(new BigDecimal("500.00"))
                .dailySlotCapacity(6)
                .active(true)
                .specialty(specialty)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        when(specialtyRepository.findById(1L)).thenReturn(Optional.of(specialty));
        when(doctorRepository.save(any(Doctor.class))).thenReturn(saved);

        DoctorResponse result = doctorService.create(request);

        assertThat(result.getId()).isEqualTo(3L);
        assertThat(result.getName()).isEqualTo("Dr. New");
        assertThat(result.getConsultationFee()).isEqualByComparingTo(new BigDecimal("500.00"));
    }

    @Test
    void testCreateDoctorThrowsWhenSpecialtyNotFound() {
        CreateDoctorRequest request = CreateDoctorRequest.builder()
                .name("Dr. New")
                .licenseNo("LIC-NEW-001")
                .consultationFee(new BigDecimal("500.00"))
                .dailySlotCapacity(6)
                .specialtyId(99L)
                .build();
        when(specialtyRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> doctorService.create(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Specialty with id 99 not found");
    }
}

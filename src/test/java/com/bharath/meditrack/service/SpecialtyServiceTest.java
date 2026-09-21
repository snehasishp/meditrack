package com.bharath.meditrack.service;

import com.bharath.meditrack.dto.CreateSpecialtyRequest;
import com.bharath.meditrack.dto.SpecialtyResponse;
import com.bharath.meditrack.model.Specialty;
import com.bharath.meditrack.repo.SpecialtyRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SpecialtyServiceTest {

    @Mock
    private SpecialtyRepository specialtyRepository;

    @InjectMocks
    private SpecialtyService specialtyService;

    @Test
    void testFindAllReturnsAllSpecialties() {
        Specialty specialty = Specialty.builder()
                .id(1L)
                .name("Cardiology")
                .slug("cardiology")
                .description("Heart care")
                .createdAt(LocalDateTime.now())
                .build();
        when(specialtyRepository.findAll()).thenReturn(List.of(specialty));

        List<SpecialtyResponse> result = specialtyService.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Cardiology");
        assertThat(result.get(0).getSlug()).isEqualTo("cardiology");
    }

    @Test
    void testCreateSpecialtySuccess() {
        CreateSpecialtyRequest request = CreateSpecialtyRequest.builder()
                .name("Dermatology")
                .slug("dermatology")
                .description("Skin care")
                .build();
        Specialty saved = Specialty.builder()
                .id(2L)
                .name("Dermatology")
                .slug("dermatology")
                .description("Skin care")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        when(specialtyRepository.save(any(Specialty.class))).thenReturn(saved);

        SpecialtyResponse result = specialtyService.create(request);

        assertThat(result.getId()).isEqualTo(2L);
        assertThat(result.getName()).isEqualTo("Dermatology");
        assertThat(result.getSlug()).isEqualTo("dermatology");
    }
}

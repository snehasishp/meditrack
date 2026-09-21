package com.bharath.meditrack.service;

import com.bharath.meditrack.dto.CreateSpecialtyRequest;
import com.bharath.meditrack.dto.SpecialtyResponse;
import com.bharath.meditrack.model.Specialty;
import com.bharath.meditrack.repo.SpecialtyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SpecialtyService {

    private final SpecialtyRepository specialtyRepository;

    public List<SpecialtyResponse> findAll() {
        return specialtyRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    public SpecialtyResponse create(CreateSpecialtyRequest request) {
        Specialty specialty = Specialty.builder()
                .name(request.getName())
                .slug(request.getSlug())
                .description(request.getDescription())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        return toResponse(specialtyRepository.save(specialty));
    }

    private SpecialtyResponse toResponse(Specialty specialty) {
        return SpecialtyResponse.builder()
                .id(specialty.getId())
                .name(specialty.getName())
                .slug(specialty.getSlug())
                .description(specialty.getDescription())
                .createdAt(specialty.getCreatedAt())
                .build();
    }
}

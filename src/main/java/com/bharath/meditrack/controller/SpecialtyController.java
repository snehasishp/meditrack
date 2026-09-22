package com.bharath.meditrack.controller;

import com.bharath.meditrack.dto.CreateSpecialtyRequest;
import com.bharath.meditrack.dto.SpecialtyResponse;
import com.bharath.meditrack.service.SpecialtyService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/specialties")
@RequiredArgsConstructor
@Tag(name = "Specialty Management", description = "Manage medical specialties")
public class SpecialtyController {

    private final SpecialtyService specialtyService;

    @GetMapping
    public List<SpecialtyResponse> all() {
        return specialtyService.findAll();
    }

    @PostMapping
    public ResponseEntity<SpecialtyResponse> create(@Valid @RequestBody CreateSpecialtyRequest request) {
        SpecialtyResponse response = specialtyService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}

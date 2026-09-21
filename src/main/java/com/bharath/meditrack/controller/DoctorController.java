package com.bharath.meditrack.controller;

import com.bharath.meditrack.dto.CreateDoctorRequest;
import com.bharath.meditrack.dto.DoctorResponse;
import com.bharath.meditrack.service.DoctorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/doctors")
@RequiredArgsConstructor
public class DoctorController {

    private final DoctorService doctorService;

    @GetMapping
    public List<DoctorResponse> all() {
        return doctorService.findAll();
    }

    @GetMapping("/{id}")
    public DoctorResponse get(@PathVariable Long id) {
        return doctorService.findById(id);
    }

    @PostMapping
    public ResponseEntity<DoctorResponse> create(@Valid @RequestBody CreateDoctorRequest request) {
        DoctorResponse response = doctorService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}

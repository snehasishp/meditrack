package com.bharath.meditrack.controller;

import com.bharath.meditrack.dto.CreatePatientRequest;
import com.bharath.meditrack.dto.PatientResponse;
import com.bharath.meditrack.service.PatientService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/patients")
@RequiredArgsConstructor
@Tag(name = "Patient Management", description = "Manage patient registration and records")
public class PatientController {

    private final PatientService patientService;

    @GetMapping
    public List<PatientResponse> all() {
        return patientService.findAll();
    }

    @PostMapping
    public ResponseEntity<PatientResponse> register(@Valid @RequestBody CreatePatientRequest request) {
        PatientResponse response = patientService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}

package com.bharath.meditrack.controller;

import com.bharath.meditrack.dto.AppointmentResponse;
import com.bharath.meditrack.dto.BookAppointmentRequest;
import com.bharath.meditrack.service.AppointmentService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/appointments")
@RequiredArgsConstructor
@Tag(name = "Appointment Management", description = "Book and manage clinic appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;

    @GetMapping
    public List<AppointmentResponse> all() {
        return appointmentService.findAll();
    }

    @GetMapping("/{id}")
    public AppointmentResponse get(@PathVariable Long id) {
        return appointmentService.findById(id);
    }

    @PostMapping("/book")
    public ResponseEntity<AppointmentResponse> book(@Valid @RequestBody BookAppointmentRequest request) {
        AppointmentResponse response = appointmentService.book(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{id}/cancel")
    public AppointmentResponse cancel(@PathVariable Long id) {
        return appointmentService.cancel(id);
    }
}

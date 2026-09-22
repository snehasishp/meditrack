package com.bharath.meditrack.service;

import com.bharath.meditrack.dto.CreateDoctorRequest;
import com.bharath.meditrack.dto.DoctorResponse;
import com.bharath.meditrack.exception.ResourceNotFoundException;
import com.bharath.meditrack.model.Doctor;
import com.bharath.meditrack.model.Specialty;
import com.bharath.meditrack.repo.DoctorRepository;
import com.bharath.meditrack.repo.SpecialtyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DoctorService {

    private final DoctorRepository doctorRepository;
    private final SpecialtyRepository specialtyRepository;

    public List<DoctorResponse> findAll() {
        return doctorRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    public DoctorResponse findById(Long id) {
        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor with id " + id + " not found"));
        return toResponse(doctor);
    }

    public DoctorResponse create(CreateDoctorRequest request) {
        Specialty specialty = specialtyRepository.findById(request.getSpecialtyId())
                .orElseThrow(() -> new ResourceNotFoundException("Specialty with id " + request.getSpecialtyId() + " not found"));

        Doctor doctor = Doctor.builder()
                .name(request.getName())
                .licenseNo(request.getLicenseNo())
                .consultationFee(request.getConsultationFee())
                .dailySlotCapacity(request.getDailySlotCapacity())
                .active(request.getActive() != null ? request.getActive() : true)
                .specialty(specialty)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        return toResponse(doctorRepository.save(doctor));
    }

    private DoctorResponse toResponse(Doctor doctor) {
        return DoctorResponse.builder()
                .id(doctor.getId())
                .name(doctor.getName())
                .licenseNo(doctor.getLicenseNo())
                .consultationFee(doctor.getConsultationFee())
                .dailySlotCapacity(doctor.getDailySlotCapacity())
                .active(doctor.isActive())
                .specialtyName(doctor.getSpecialty() != null ? doctor.getSpecialty().getName() : null)
                .createdAt(doctor.getCreatedAt())
                .averageRating(getAverageRating(doctor.getId()))
                .build();
    }

    private Double getAverageRating(Long doctorId) {
        return doctorRepository.getAverageRatingByDoctorId(doctorId);
    }
}

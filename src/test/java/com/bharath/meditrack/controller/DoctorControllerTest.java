package com.bharath.meditrack.controller;

import com.bharath.meditrack.dto.CreateDoctorRequest;
import com.bharath.meditrack.dto.DoctorResponse;
import com.bharath.meditrack.exception.GlobalExceptionHandler;
import com.bharath.meditrack.exception.ResourceNotFoundException;
import com.bharath.meditrack.service.DoctorService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class DoctorControllerTest {

    private MockMvc mockMvc;

    @Mock
    private DoctorService doctorService;

    @InjectMocks
    private DoctorController doctorController;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(doctorController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void testGetAllDoctorsReturnsOk() throws Exception {
        DoctorResponse response = DoctorResponse.builder()
                .id(1L).name("Dr. Asha Rao").licenseNo("LIC-CARD-001")
                .consultationFee(new BigDecimal("600.00")).dailySlotCapacity(8).active(true)
                .specialtyName("Cardiology").build();
        when(doctorService.findAll()).thenReturn(List.of(response));

        mockMvc.perform(get("/api/v1/doctors"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$[0].id").value(1))
               .andExpect(jsonPath("$[0].name").value("Dr. Asha Rao"))
               .andExpect(jsonPath("$[0].specialtyName").value("Cardiology"));
    }

    @Test
    void testGetDoctorByIdReturnsOk() throws Exception {
        DoctorResponse response = DoctorResponse.builder()
                .id(1L).name("Dr. Asha Rao").licenseNo("LIC-CARD-001")
                .consultationFee(new BigDecimal("600.00")).dailySlotCapacity(8).active(true).build();
        when(doctorService.findById(1L)).thenReturn(response);

        mockMvc.perform(get("/api/v1/doctors/1"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.id").value(1))
               .andExpect(jsonPath("$.name").value("Dr. Asha Rao"));
    }

    @Test
    void testGetDoctorByIdReturnsNotFound() throws Exception {
        when(doctorService.findById(99L))
                .thenThrow(new ResourceNotFoundException("Doctor with id 99 not found"));

        mockMvc.perform(get("/api/v1/doctors/99"))
               .andExpect(status().isNotFound())
               .andExpect(jsonPath("$.status").value(404))
               .andExpect(jsonPath("$.message").value("Doctor with id 99 not found"));
    }

    @Test
    void testCreateDoctorReturnsCreated() throws Exception {
        CreateDoctorRequest request = CreateDoctorRequest.builder()
                .name("Dr. New").licenseNo("LIC-NEW-001")
                .consultationFee(new BigDecimal("500.00")).dailySlotCapacity(6)
                .active(true).specialtyId(1L).build();
        DoctorResponse response = DoctorResponse.builder()
                .id(3L).name("Dr. New").licenseNo("LIC-NEW-001")
                .consultationFee(new BigDecimal("500.00")).dailySlotCapacity(6).active(true)
                .specialtyName("Cardiology").build();
        when(doctorService.create(any(CreateDoctorRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/doctors")
                       .contentType(MediaType.APPLICATION_JSON)
                       .content(objectMapper.writeValueAsString(request)))
               .andExpect(status().isCreated())
               .andExpect(jsonPath("$.id").value(3))
               .andExpect(jsonPath("$.name").value("Dr. New"));
    }
}

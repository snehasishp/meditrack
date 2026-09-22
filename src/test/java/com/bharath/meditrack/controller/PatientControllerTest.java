package com.bharath.meditrack.controller;

import com.bharath.meditrack.dto.CreatePatientRequest;
import com.bharath.meditrack.dto.PatientResponse;
import com.bharath.meditrack.exception.GlobalExceptionHandler;
import com.bharath.meditrack.service.PatientService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class PatientControllerTest {

    private MockMvc mockMvc;

    @Mock
    private PatientService patientService;

    @InjectMocks
    private PatientController patientController;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setObjectMapper(objectMapper);

        mockMvc = MockMvcBuilders.standaloneSetup(patientController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setMessageConverters(converter)
                .build();
    }

    @Test
    void testGetAllPatientsReturnsOk() throws Exception {
        PatientResponse response = PatientResponse.builder()
                .id(1L).firstName("John").lastName("Doe").email("john@example.com").build();
        when(patientService.findAll()).thenReturn(List.of(response));

        mockMvc.perform(get("/api/v1/patients"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$[0].id").value(1))
               .andExpect(jsonPath("$[0].firstName").value("John"))
               .andExpect(jsonPath("$[0].email").value("j***@example.com"));
    }

    @Test
    void testCreatePatientReturnsCreated() throws Exception {
        PatientResponse response = PatientResponse.builder()
                .id(2L).firstName("Jane").lastName("Smith").email("jane@example.com").phone("+1987654321").build();
        doReturn(response).when(patientService).create(any(CreatePatientRequest.class));

        String json = """
                {"firstName":"Jane","lastName":"Smith","email":"jane@example.com","phone":"+1987654321"}
                """;

        mockMvc.perform(post("/api/v1/patients")
                       .contentType(MediaType.APPLICATION_JSON)
                       .content(json))
               .andExpect(status().isCreated())
               .andExpect(jsonPath("$.id").value(2))
               .andExpect(jsonPath("$.firstName").value("Jane"))
               .andExpect(jsonPath("$.email").value("j***@example.com"));
    }
}

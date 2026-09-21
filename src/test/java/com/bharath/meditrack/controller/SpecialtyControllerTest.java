package com.bharath.meditrack.controller;

import com.bharath.meditrack.dto.CreateSpecialtyRequest;
import com.bharath.meditrack.dto.SpecialtyResponse;
import com.bharath.meditrack.exception.GlobalExceptionHandler;
import com.bharath.meditrack.service.SpecialtyService;
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

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class SpecialtyControllerTest {

    private MockMvc mockMvc;

    @Mock
    private SpecialtyService specialtyService;

    @InjectMocks
    private SpecialtyController specialtyController;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(specialtyController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void testGetAllSpecialtiesReturnsOk() throws Exception {
        SpecialtyResponse response = SpecialtyResponse.builder()
                .id(1L).name("Cardiology").slug("cardiology").description("Heart care").build();
        when(specialtyService.findAll()).thenReturn(List.of(response));

        mockMvc.perform(get("/api/v1/specialties"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$[0].id").value(1))
               .andExpect(jsonPath("$[0].name").value("Cardiology"))
               .andExpect(jsonPath("$[0].slug").value("cardiology"));
    }

    @Test
    void testCreateSpecialtyReturnsCreated() throws Exception {
        CreateSpecialtyRequest request = CreateSpecialtyRequest.builder()
                .name("Dermatology").slug("dermatology").description("Skin care").build();
        SpecialtyResponse response = SpecialtyResponse.builder()
                .id(2L).name("Dermatology").slug("dermatology").description("Skin care").build();
        when(specialtyService.create(any(CreateSpecialtyRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/specialties")
                       .contentType(MediaType.APPLICATION_JSON)
                       .content(objectMapper.writeValueAsString(request)))
               .andExpect(status().isCreated())
               .andExpect(jsonPath("$.id").value(2))
               .andExpect(jsonPath("$.name").value("Dermatology"));
    }
}

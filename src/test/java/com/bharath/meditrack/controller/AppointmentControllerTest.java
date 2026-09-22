package com.bharath.meditrack.controller;

import com.bharath.meditrack.dto.AppointmentResponse;
import com.bharath.meditrack.dto.BookAppointmentRequest;
import com.bharath.meditrack.dto.CreateFeedbackRequest;
import com.bharath.meditrack.dto.FeedbackResponse;
import com.bharath.meditrack.exception.BusinessRuleException;
import com.bharath.meditrack.exception.DuplicateResourceException;
import com.bharath.meditrack.exception.GlobalExceptionHandler;
import com.bharath.meditrack.exception.ResourceNotFoundException;
import com.bharath.meditrack.service.AppointmentService;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AppointmentControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AppointmentService appointmentService;

    @InjectMocks
    private AppointmentController appointmentController;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setObjectMapper(objectMapper);

        mockMvc = MockMvcBuilders.standaloneSetup(appointmentController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setMessageConverters(converter)
                .build();
    }

    @Test
    void testGetAllAppointmentsReturnsOk() throws Exception {
        AppointmentResponse response = AppointmentResponse.builder()
                .appointmentId(1L).appointmentNo("APT-12345678")
                .patientId(1L).patientName("John Doe")
                .doctorId(1L).doctorName("Dr. Asha Rao")
                .status("REQUESTED")
                .scheduledDate(LocalDate.of(2026, 9, 25))
                .totalAmount(new BigDecimal("600.00"))
                .createdAt(LocalDateTime.now()).build();
        when(appointmentService.findAll()).thenReturn(List.of(response));

        mockMvc.perform(get("/api/v1/appointments"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$[0].appointmentId").value(1))
               .andExpect(jsonPath("$[0].status").value("REQUESTED"))
               .andExpect(jsonPath("$[0].patientName").value("John Doe"));
    }

    @Test
    void testGetAppointmentByIdReturnsOk() throws Exception {
        AppointmentResponse response = AppointmentResponse.builder()
                .appointmentId(1L).status("REQUESTED").build();
        when(appointmentService.findById(1L)).thenReturn(response);

        mockMvc.perform(get("/api/v1/appointments/1"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.appointmentId").value(1));
    }

    @Test
    void testGetAppointmentByIdReturnsNotFound() throws Exception {
        when(appointmentService.findById(99L))
                .thenThrow(new ResourceNotFoundException("Appointment with id 99 not found"));

        mockMvc.perform(get("/api/v1/appointments/99"))
               .andExpect(status().isNotFound())
               .andExpect(jsonPath("$.status").value(404))
               .andExpect(jsonPath("$.message").value("Appointment with id 99 not found"));
    }

    @Test
    void testBookAppointmentReturnsCreated() throws Exception {
        BookAppointmentRequest request = BookAppointmentRequest.builder()
                .patientId(1L).doctorId(1L).date(LocalDate.of(2026, 9, 25)).build();
        AppointmentResponse response = AppointmentResponse.builder()
                .appointmentId(1L).appointmentNo("APT-12345678")
                .patientId(1L).doctorId(1L)
                .status("REQUESTED").totalAmount(new BigDecimal("600.00")).build();
        when(appointmentService.book(any(BookAppointmentRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/appointments/book")
                       .contentType(MediaType.APPLICATION_JSON)
                       .content(objectMapper.writeValueAsString(request)))
               .andExpect(status().isCreated())
               .andExpect(jsonPath("$.appointmentId").value(1))
               .andExpect(jsonPath("$.status").value("REQUESTED"));
    }

    @Test
    void testBookAppointmentWithNoSlotsReturnsConflict() throws Exception {
        BookAppointmentRequest request = BookAppointmentRequest.builder()
                .patientId(1L).doctorId(1L).date(LocalDate.of(2026, 9, 25)).build();
        when(appointmentService.book(any(BookAppointmentRequest.class)))
                .thenThrow(new BusinessRuleException("No slots available for doctor Dr. Asha Rao on 2026-09-25"));

        mockMvc.perform(post("/api/v1/appointments/book")
                       .contentType(MediaType.APPLICATION_JSON)
                       .content(objectMapper.writeValueAsString(request)))
               .andExpect(status().isConflict())
               .andExpect(jsonPath("$.status").value(409))
               .andExpect(jsonPath("$.error").value("Conflict"));
    }

    @Test
    void testCancelAppointmentReturnsOk() throws Exception {
        AppointmentResponse response = AppointmentResponse.builder()
                .appointmentId(1L).status("CANCELLED").build();
        when(appointmentService.cancel(1L)).thenReturn(response);

        mockMvc.perform(post("/api/v1/appointments/1/cancel"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.appointmentId").value(1))
               .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    @Test
    void testCancelAlreadyCancelledAppointmentReturnsConflict() throws Exception {
        when(appointmentService.cancel(1L))
                .thenThrow(new BusinessRuleException("Appointment cannot be cancelled in status CANCELLED"));

        mockMvc.perform(post("/api/v1/appointments/1/cancel"))
               .andExpect(status().isConflict())
               .andExpect(jsonPath("$.status").value(409))
               .andExpect(jsonPath("$.message").value("Appointment cannot be cancelled in status CANCELLED"));
    }

    @Test
    void testSubmitFeedbackReturnsCreated() throws Exception {
        CreateFeedbackRequest request = CreateFeedbackRequest.builder()
                .rating(5)
                .comment("Great service!")
                .build();
        FeedbackResponse response = FeedbackResponse.builder()
                .id(1L)
                .appointmentId(1L)
                .rating(5)
                .comment("Great service!")
                .createdAt(LocalDateTime.now())
                .build();
        when(appointmentService.submitFeedback(eq(1L), any(CreateFeedbackRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/appointments/1/feedback")
                       .contentType(MediaType.APPLICATION_JSON)
                       .content(objectMapper.writeValueAsString(request)))
               .andExpect(status().isCreated())
               .andExpect(jsonPath("$.id").value(1))
               .andExpect(jsonPath("$.rating").value(5))
               .andExpect(jsonPath("$.comment").value("Great service!"));
    }

    @Test
    void testSubmitFeedbackOnNonCompletedReturnsConflict() throws Exception {
        CreateFeedbackRequest request = CreateFeedbackRequest.builder().rating(5).build();
        when(appointmentService.submitFeedback(eq(1L), any(CreateFeedbackRequest.class)))
                .thenThrow(new BusinessRuleException("Appointment must be completed to submit feedback"));

        mockMvc.perform(post("/api/v1/appointments/1/feedback")
                       .contentType(MediaType.APPLICATION_JSON)
                       .content(objectMapper.writeValueAsString(request)))
               .andExpect(status().isConflict())
               .andExpect(jsonPath("$.status").value(409))
               .andExpect(jsonPath("$.message").value("Appointment must be completed to submit feedback"));
    }

    @Test
    void testSubmitFeedbackAlreadyExistsReturnsConflict() throws Exception {
        CreateFeedbackRequest request = CreateFeedbackRequest.builder().rating(5).build();
        when(appointmentService.submitFeedback(eq(1L), any(CreateFeedbackRequest.class)))
                .thenThrow(new DuplicateResourceException("Feedback already submitted for appointment 1"));

        mockMvc.perform(post("/api/v1/appointments/1/feedback")
                       .contentType(MediaType.APPLICATION_JSON)
                       .content(objectMapper.writeValueAsString(request)))
               .andExpect(status().isConflict())
               .andExpect(jsonPath("$.status").value(409))
               .andExpect(jsonPath("$.message").value("Feedback already submitted for appointment 1"));
    }
}

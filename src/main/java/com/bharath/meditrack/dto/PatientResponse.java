package com.bharath.meditrack.dto;

import lombok.*;

import java.time.LocalDate;
import java.time.Period;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PatientResponse {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private LocalDate dateOfBirth;

    public String getEmail() {
        return email != null ? maskEmail(email) : null;
    }

    public String getPhone() {
        return phone != null ? maskPhone(phone) : null;
    }

    public Integer getAge() {
        return dateOfBirth != null
                ? Period.between(dateOfBirth, LocalDate.now()).getYears()
                : null;
    }

    private static String maskEmail(String email) {
        int at = email.indexOf('@');
        if (at <= 1) {
            return email;
        }
        return email.charAt(0) + "***" + email.substring(at);
    }

    private static String maskPhone(String phone) {
        if (phone.length() <= 4) {
            return "***-" + phone;
        }
        return phone.substring(0, phone.length() - 4).replaceAll(".", "*") + phone.substring(phone.length() - 4);
    }
}

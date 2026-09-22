package com.bharath.meditrack.repo;

import com.bharath.meditrack.model.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
    boolean existsByAppointmentId(Long appointmentId);
    Optional<Feedback> findByAppointmentId(Long appointmentId);
}

package com.bharath.meditrack.repo;

import com.bharath.meditrack.model.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
    @Query("SELECT CASE WHEN COUNT(f) > 0 THEN true ELSE false END FROM Feedback f WHERE f.appointment.appointmentId = :appointmentId")
    boolean existsByAppointmentId(@Param("appointmentId") Long appointmentId);

    @Query("SELECT f FROM Feedback f WHERE f.appointment.appointmentId = :appointmentId")
    Optional<Feedback> findByAppointmentId(@Param("appointmentId") Long appointmentId);
}

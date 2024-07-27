package com.example.healthcare.application.exercise.repository;

import com.example.healthcare.application.exercise.domain.ExerciseFeedback;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExerciseFeedbackRepository extends JpaRepository<ExerciseFeedback, Long> {
}

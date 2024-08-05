package com.example.healthcare.application.exercise.service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record ExerciseFeedbackDTO(

        @NotNull
        Long trainer_id,
        @NotNull
        Long exercise_id,

        String feedbackMessage
) {
    }

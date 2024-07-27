package com.example.healthcare.application.exercise.service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ExerciseFeedbackDTO(
        // 운동 id, 영상, 요청자의 메시지
        @NotNull
        Long exercise_id,
        @NotBlank
        String videoUrl,

        String feedbackMessage
) {
    }

package com.example.healthcare.application.exercise.controller;

import com.example.healthcare.application.exercise.service.ExerciseCmService;
import com.example.healthcare.application.exercise.service.dto.ExerciseFeedbackDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/cm/exercises")
public class ExerciseCmController {

    private final ExerciseCmService exerciseCmService;

    // 운동 피드백 요청
    @PostMapping("/{user-id}")
    public void getUser(@PathVariable(value = "user-id") Long userId,
                        @Valid @RequestBody ExerciseFeedbackDTO dto) {
        exerciseCmService.exerciseFeedback(userId, dto);
    }
}

package com.example.healthcare.application.exercise.service;

import com.example.healthcare.application.exercise.domain.Exercise;
import com.example.healthcare.application.exercise.domain.ExerciseFeedback;
import com.example.healthcare.application.exercise.repository.ExerciseFeedbackRepository;
import com.example.healthcare.application.exercise.repository.ExerciseRepository;
import com.example.healthcare.application.exercise.service.dto.ExerciseFeedbackDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ExerciseCmService {

    private final ExerciseFeedbackRepository exerciseFeedbackRepository;


    @Transactional
    public void exerciseFeedback(Long userId, ExerciseFeedbackDTO dto) {
//        ExerciseFeedback exerciseFeedback = new ExerciseFeedback();

    }
}

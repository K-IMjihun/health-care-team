package com.example.healthcare.application.exercise.service;

import com.example.healthcare.application.exercise.domain.Exercise;
import com.example.healthcare.application.exercise.domain.ExerciseFeedback;
import com.example.healthcare.application.exercise.repository.ExerciseFeedbackRepository;
import com.example.healthcare.application.exercise.repository.ExerciseRepository;
import com.example.healthcare.application.exercise.service.dto.ExerciseFeedbackDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ExerciseCmService {

    private final ExerciseFeedbackRepository exerciseFeedbackRepository;

    @Transactional
    public void exerciseFeedback(Long userId, List<MultipartFile> feedbackVideo, ExerciseFeedbackDTO dto) {
        if (feedbackVideo == null) throw new IllegalArgumentException("FeedbackVideo cannot be null");
        // 1. 비디오 s3 저장
        // 2. url 반환
        // 3. 반환한 url 들을 feedback에 list로 저장

        ExerciseFeedback exerciseFeedback = new ExerciseFeedback(userId, dto);

    }
}

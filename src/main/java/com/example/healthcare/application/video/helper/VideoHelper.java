package com.example.healthcare.application.video.helper;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.example.healthcare.application.exercise.domain.Exercise;
import com.example.healthcare.application.exercise.domain.ExerciseFeedback;
import com.example.healthcare.application.video.domain.FeedbackVideo;
import com.example.healthcare.application.video.repository.VideoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class VideoHelper {

    private final VideoRepository videoRepository;
    private final AmazonS3 amazonS3;
    private final String bucket;

    @Transactional
    public List<FeedbackVideo> saveFeedbackVideoToS3(List<MultipartFile> videos, Exercise exercise, ExerciseFeedback exerciseFeedback) {
        for (MultipartFile video : videos) {
            if (!validateFile(video)) throw new IllegalStateException("File validation failed");
        }

        List<FeedbackVideo> FeedbackVideoList = new ArrayList<>();
        // 새 S3 객체 업로드
        for (MultipartFile video : videos) {

            String videoName = Optional.ofNullable(video.getOriginalFilename())
                    .orElseThrow(() -> new IllegalArgumentException("Original filename is null"));// 파일의 원본명
            String extension = StringUtils.getFilenameExtension(Paths.get(videoName).toString()); // 확장자명
            String videoUuid = UUID.randomUUID() + "." + extension; // 해당 파일의 고유한 이름

            // 업로드할 파일의 메타데이터 생성(확장자 / 파일 크기.byte)
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType("image/" + extension);
            metadata.setContentLength(video.getSize());

            // 요청 객체 생성(버킷명, 파일명, 스트림, 메타정보)
            PutObjectRequest request;
            try {
                request = new PutObjectRequest(bucket, videoUuid, video.getInputStream(), metadata);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            FeedbackVideo feedbackVideo = new FeedbackVideo(
                    videoUuid,
                    amazonS3.getUrl(bucket, videoUuid).toString(),
                    exercise,
                    exerciseFeedback);
            // S3 버킷에 등록
            amazonS3.putObject(request);
            FeedbackVideoList.add(feedbackVideo);

        }
        return FeedbackVideoList;
    }

    @Transactional
    public void saveFeedbackVideoToRepository(List<FeedbackVideo> feedbackVideos){
        videoRepository.saveAll(feedbackVideos);
    }

    public boolean validateFile(MultipartFile file) {
        // 지원하는 파일 확장자 리스트
        List<String> fileExtensions = Arrays.asList("jpg", "png", "webp", "heif", "gif", "jpeg", "mp4", "avi", "mov");

        String path = Paths.get(Objects.requireNonNull(file.getOriginalFilename())).toString(); // 원본 파일명으로 파일 경로 생성
        String extension = StringUtils.getFilenameExtension(path); // 확장자명

        // 파일 확장자 null check
        if (extension == null) throw new IllegalArgumentException("File extension cannot be null.");

        // 파일 확장자 검증
        if (!fileExtensions.contains(extension.toLowerCase())) {
            throw new IllegalArgumentException("Unsupported file extension format.");
        }

        // 파일 크기 검증
        long maxSize = 10 * 1024 * 1024; // 10MB
        long fileSize = file.getSize();

        if (fileSize > maxSize) throw new IllegalArgumentException("File size must not exceed 10MB.");

        return true;
    }


}


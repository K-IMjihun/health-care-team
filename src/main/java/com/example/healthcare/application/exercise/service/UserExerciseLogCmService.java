package com.example.healthcare.application.exercise.service;

import com.example.healthcare.application.account.domain.User;
import com.example.healthcare.application.account.domain.code.UserStatus;
import com.example.healthcare.application.account.helper.UserHelper;
import com.example.healthcare.application.account.repository.UserRepository;
import com.example.healthcare.application.common.exception.DuplicateException;
import com.example.healthcare.application.common.exception.DuplicateException.DuplicateExceptionCode;
import com.example.healthcare.application.common.exception.InvalidInputValueException;
import com.example.healthcare.application.common.exception.InvalidInputValueException.InvalidInputValueExceptionCode;
import com.example.healthcare.application.common.exception.ResourceException;
import com.example.healthcare.application.common.exception.ResourceException.ResourceExceptionCode;
import com.example.healthcare.application.exercise.controller.dto.CreateUserExerciseLogDTO;
import com.example.healthcare.application.exercise.controller.dto.CreateUserExerciseRoutineDTO;
import com.example.healthcare.application.exercise.controller.dto.UpdateUserExerciseLogDTO;
import com.example.healthcare.application.exercise.domain.Exercise;
import com.example.healthcare.application.exercise.domain.ExerciseTypeRelation;
import com.example.healthcare.application.exercise.domain.UserExerciseLog;
import com.example.healthcare.application.exercise.domain.UserExerciseRoutine;
import com.example.healthcare.application.exercise.domain.code.ExerciseType;
import com.example.healthcare.application.exercise.exception.ExerciseException;
import com.example.healthcare.application.exercise.exception.ExerciseException.ExerciseExceptionCode;
import com.example.healthcare.application.exercise.helper.ExerciseHelper;
import com.example.healthcare.application.exercise.repository.ExerciseRepository;
import com.example.healthcare.application.exercise.repository.ExerciseTypeRelationRepository;
import com.example.healthcare.application.exercise.repository.UserExerciseLogRepository;
import com.example.healthcare.application.exercise.repository.UserExerciseRoutineRepository;
import com.example.healthcare.application.exercise.repository.UserExerciseSetRepository;
import com.example.healthcare.application.exercise.service.data.UserExerciseData.UserExerciseLogData;
import com.example.healthcare.application.vo.UserExerciseLogDetailVO;
import com.example.healthcare.application.vo.UserExerciseLogSummaryVO;
import com.example.healthcare.application.vo.UserExerciseLogVO;
import com.example.healthcare.application.vo.UserExerciseRoutineVO;
import com.example.healthcare.application.vo.UserExerciseSetVO;
import com.example.healthcare.infra.config.security.user.LoginUser;
import com.example.healthcare.util.VerifyUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.math.BigInteger;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserExerciseLogCmService {

  private static final PageRequest DEFAULT_PAGE_REQUEST = PageRequest.of(0, VerifyUtil.MAX_PAGE_SIZE);

  private final UserHelper userHelper;
  private final ExerciseHelper exerciseHelper;
  private final UserRepository userRepository;
  private final ExerciseRepository exerciseRepository;
  private final ExerciseTypeRelationRepository exerciseTypeRelationRepository;
  private final UserExerciseLogRepository userExerciseLogRepository;
  private final UserExerciseRoutineRepository userExerciseRoutineRepository;
  private final UserExerciseSetRepository userExerciseSetRepository;

  @Transactional
  public void createExerciseLog(LoginUser loginUser, CreateUserExerciseLogDTO dto) {
    User user = userRepository.findByIdAndUserStatusIs(loginUser.getId(), UserStatus.ACTIVATED)
      .orElseThrow(() -> new ResourceException(ResourceExceptionCode.RESOURCE_NOT_FOUND));

    Integer exerciseCount = 0;
    BigInteger totalSetCount = null;
    BigInteger totalWeight = null;
    BigInteger totalReps = null;
    BigInteger totalTime = null;

    // 운동 종록(routine) + 운동 세트(set)
    List<Long> exerciseIds = dto.routineDTOList().stream()
      .map(CreateUserExerciseRoutineDTO::exerciseId)
      .toList();
    Map<Long, Exercise> exercises = exerciseRepository.findAllByIdIn(exerciseIds)
      .stream().collect(Collectors.toMap(Exercise::getId, exercise -> exercise));
    // 각 exercise 관련 exerciseType 들 조회
    Map<Exercise, List<ExerciseTypeRelation>> exerciseTypeMap = exerciseTypeRelationRepository.findAllByExerciseIdIn(
        exercises.keySet())
      .stream()
      .collect(Collectors.groupingBy(ExerciseTypeRelation::getExercise));

    UserExerciseLogData logData = new UserExerciseLogData();

    int routineSize = dto.routineDTOList().size();
    Set<Integer> routineOrderMemo = new HashSet<>();

    List<UserExerciseRoutine> routineEntityList = dto.routineDTOList()
      .stream()
      .map(routineDTO -> {
        if (routineOrderMemo.contains(routineDTO.order())) {
          throw new DuplicateException(DuplicateExceptionCode.DUPLICATE_ROUTINE_ORDER);
        }
        routineOrderMemo.add(routineDTO.order());

        if (routineDTO.order() > routineSize) {
          throw new InvalidInputValueException(InvalidInputValueExceptionCode.INVALID_INPUT_VALUE);
        }

        Exercise exercise = exercises.get(routineDTO.exerciseId());
        if (exercise == null) {
          throw new ResourceException(ResourceExceptionCode.RESOURCE_NOT_FOUND);
        }

        Set<ExerciseType> exerciseTypes = exerciseTypeMap.get(exercise.getId())
          .stream()
          .map(ExerciseTypeRelation::getExerciseType)
          .collect(Collectors.toSet());
        if (CollectionUtils.isEmpty(exerciseTypes)) {
          throw new ExerciseException(ExerciseExceptionCode.NOT_FOUND_RELATION_EXERCISE_TYPE);
        }

        return exerciseHelper.createUserExerciseRoutineAndSet(routineDTO, exercise, exerciseTypes, logData);
      })
      .toList();

    // 운동 기록(log)
    UserExerciseLog logEntity = UserExerciseLog.createLog(dto, user, logData);
    userExerciseLogRepository.save(logEntity);

    routineEntityList.forEach(routine -> routine.applyLog(logEntity));
    userExerciseRoutineRepository.saveAll(routineEntityList);
    userExerciseSetRepository.saveAll(logData.setEntityList);
  }

  // Paging Max Size 28 ~ 31
  public Page<UserExerciseLogVO> getExerciseLogMonthly(LoginUser loginUser, Integer year, Integer month) {
    User user = userRepository.findByIdAndUserStatusIs(loginUser.getId(), UserStatus.ACTIVATED)
      .orElseThrow(() -> new ResourceException(ResourceExceptionCode.RESOURCE_NOT_FOUND));

    return userExerciseLogRepository.findExerciseLogMonthly(user, year, month, DEFAULT_PAGE_REQUEST);
  }

  // Paging (max 2)
  public Page<UserExerciseLogSummaryVO> getExerciseLogDaily(LoginUser loginUser, Integer year, Integer month, Integer day) {
    User user = userRepository.findByIdAndUserStatusIs(loginUser.getId(), UserStatus.ACTIVATED)
      .orElseThrow(() -> new ResourceException(ResourceExceptionCode.RESOURCE_NOT_FOUND));

    return userExerciseLogRepository.findExerciseLogDaily(user, year, month, day, DEFAULT_PAGE_REQUEST);
  }

  // log calc info + inner (routine + set) info
  public UserExerciseLogDetailVO getExerciseLogDetail(LoginUser loginUser, Long exerciseLogId) {
    User user = userRepository.findByIdAndUserStatusIs(loginUser.getId(), UserStatus.ACTIVATED)
      .orElseThrow(() -> new ResourceException(ResourceExceptionCode.RESOURCE_NOT_FOUND));

    UserExerciseLog userExerciseLog = userExerciseLogRepository.findById(exerciseLogId)
      .orElseThrow(() -> new ResourceException(ResourceExceptionCode.RESOURCE_NOT_FOUND));

    userHelper.checkAuthorization(user, userExerciseLog.getUser());

    if (userExerciseLog.isDeleted()) {
      throw new ExerciseException(ExerciseExceptionCode.DELETED_EXERCISE_LOG);
    }

    // get related routine and related set
    Map<Long, List<UserExerciseSetVO>> routineSetMaps = userExerciseLogRepository.findExerciseSetAllByLog(
        userExerciseLog, DEFAULT_PAGE_REQUEST)
      .stream()
      .collect(Collectors.groupingBy(UserExerciseSetVO::routineId));

    List<UserExerciseRoutineVO> routines = userExerciseLogRepository.findExerciseRoutineAllByLog(
        userExerciseLog, DEFAULT_PAGE_REQUEST)
      .stream()
      .peek(routine -> routine.setExerciseSetList(routineSetMaps.get(routine.getRoutineId())))
      .toList();

    return UserExerciseLogDetailVO.valueOf(userExerciseLog, routines);
  }

  @Transactional
  public void updateExerciseLog(LoginUser loginUser, Long exerciseLogId, UpdateUserExerciseLogDTO dto) {
    // 삭제된 routine 없는지 확인
    // 삭제된 set 없는지 확인
  }

  @Transactional
  public void deleteExerciseLog(LoginUser loginUser, Long exerciseLogId) {
    User user = userRepository.findByIdAndUserStatusIs(loginUser.getId(), UserStatus.ACTIVATED)
      .orElseThrow(() -> new ResourceException(ResourceExceptionCode.RESOURCE_NOT_FOUND));

    UserExerciseLog userExerciseLog = userExerciseLogRepository.findById(exerciseLogId)
      .orElseThrow(() -> new ResourceException(ResourceExceptionCode.RESOURCE_NOT_FOUND));

    userHelper.checkAuthorization(user, userExerciseLog.getUser());

    if (userExerciseLog.isDeleted()) {
      throw new ExerciseException(ExerciseExceptionCode.DELETED_EXERCISE_LOG);
    }

    // UserExerciseLog Related UserExerciseRoutine delete(update)
    userExerciseRoutineRepository.deleteAllByUserExerciseLogIdQuery(userExerciseLog.getId());

    // UserExerciseLog delete(update)
    userExerciseLogRepository.deleteById(exerciseLogId);
  }

}

package com.example.healthcare.application.exercise.helper;

import com.example.healthcare.application.account.domain.User;
import com.example.healthcare.application.common.exception.DuplicateException;
import com.example.healthcare.application.common.exception.DuplicateException.DuplicateExceptionCode;
import com.example.healthcare.application.common.exception.InvalidInputValueException;
import com.example.healthcare.application.common.exception.InvalidInputValueException.InvalidInputValueExceptionCode;
import com.example.healthcare.application.exercise.controller.dto.CreateExerciseDTO;
import com.example.healthcare.application.exercise.controller.dto.CreateUserExerciseRoutineDTO;
import com.example.healthcare.application.exercise.domain.Exercise;
import com.example.healthcare.application.exercise.domain.ExerciseTypeRelation;
import com.example.healthcare.application.exercise.domain.UserExerciseRoutine;
import com.example.healthcare.application.exercise.domain.UserExerciseSet;
import com.example.healthcare.application.exercise.domain.code.ExerciseType;
import com.example.healthcare.application.exercise.exception.ExerciseException;
import com.example.healthcare.application.exercise.exception.ExerciseException.ExerciseExceptionCode;
import com.example.healthcare.application.exercise.repository.ExerciseRepository;
import com.example.healthcare.application.exercise.repository.ExerciseTypeRelationRepository;
import com.example.healthcare.application.exercise.service.data.UserExerciseData.UserExerciseLogData;
import com.example.healthcare.application.exercise.service.data.UserExerciseData.UserExerciseRoutineData;
import com.example.healthcare.application.vo.ExerciseDetailVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ExerciseHelper {

  private final ExerciseRepository exerciseRepository;
  private final ExerciseTypeRelationRepository exerciseTypeRelationRepository;

  @Transactional
  public void createExercise(User user, CreateExerciseDTO dto) {
    Exercise exercise = Exercise.createExercise(dto, user);
    exerciseRepository.save(exercise);

    List<ExerciseTypeRelation> relations = new ArrayList<>();
    for (ExerciseType exerciseType : dto.exerciseTypes()) {
      relations.add(ExerciseTypeRelation.createExerciseTypeRelation(exerciseType, exercise));
    }
    exerciseTypeRelationRepository.saveAll(relations);
  }

  @Transactional(readOnly = true)
  public ExerciseDetailVO getExercise(Exercise exercise) {
    List<ExerciseTypeRelation> exerciseTypeRelations = exerciseTypeRelationRepository.findAllByExercise(exercise);
    if (CollectionUtils.isEmpty(exerciseTypeRelations)) {
      throw new ExerciseException(ExerciseExceptionCode.NOT_FOUND_RELATION_EXERCISE_TYPE);
    }

    return ExerciseDetailVO.valueOf(exercise, exerciseTypeRelations);
  }

  @Transactional
  public UserExerciseRoutine createUserExerciseRoutineAndSet(CreateUserExerciseRoutineDTO dto,
    Exercise exercise, UserExerciseLogData userExerciseLogData) {
    Set<ExerciseType> exerciseTypes = exerciseTypeRelationRepository.findAllByExercise(exercise)
      .stream()
      .map(ExerciseTypeRelation::getExerciseType)
      .collect(Collectors.toSet());
    if (CollectionUtils.isEmpty(exerciseTypes)) {
      throw new ExerciseException(ExerciseExceptionCode.NOT_FOUND_RELATION_EXERCISE_TYPE);
    }

    UserExerciseRoutineData routineData = new UserExerciseRoutineData();

    int totalSetSize = dto.setDTOList().size();
    Set<Long> setNumberMemo = new HashSet<>();
    List<UserExerciseSet> setList = dto.setDTOList().stream()
      .map(setDTO -> {
        // setNumber validation
        if (setDTO.serNumber() > totalSetSize) {
          throw new InvalidInputValueException(InvalidInputValueExceptionCode.INVALID_INPUT_VALUE);
        }

        if (setNumberMemo.contains(setDTO.serNumber())) {
          throw new DuplicateException(DuplicateExceptionCode.DUPLICATE_SET_NUMBER);
        }
        setNumberMemo.add(setDTO.serNumber());

        // exerciseType validation
        checkExerciseType(exerciseTypes, ExerciseType.WEIGHT, setDTO.weight());
        checkExerciseType(exerciseTypes, ExerciseType.REPS, setDTO.reps());
        checkExerciseType(exerciseTypes, ExerciseType.TIME, setDTO.time());

        UserExerciseSet setEntity = UserExerciseSet.createSet(setDTO);
        routineData.update(setDTO, dto.weightUnitType());
        return setEntity;
      }).toList();

    UserExerciseRoutine routineEntity = UserExerciseRoutine.createRoutine(dto, routineData, exercise);

    setList.forEach(set -> set.applyRoutine(routineEntity));

    userExerciseLogData.setEntityList.addAll(setList);
    userExerciseLogData.update(routineData);
    return routineEntity;
  }

  private void checkExerciseType(Set<ExerciseType> types, ExerciseType type, Object value) {
    if ((types.contains(type) && Objects.isNull(value)) || (!types.contains(type) && Objects.nonNull(value))) {
      throw new InvalidInputValueException(InvalidInputValueExceptionCode.INVALID_INPUT_VALUE);
    }
  }

}

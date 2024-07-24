package com.example.healthcare.account.service.dto;

import com.example.healthcare.util.RegexUtil;
import com.example.healthcare.util.VerifyUtil;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.hibernate.validator.constraints.Length;

public record CreateUserDTO(
  @NotBlank
  @Length(min = VerifyUtil.MIN_DEFAULT_LENGTH, max = VerifyUtil.MAX_EMAIL_LENGTH)
  @Email(regexp = RegexUtil.REGEXP_EMAIL)
  String email,
  @NotBlank
  @Length(max = VerifyUtil.MAX_DEFAULT_LENGTH)
  @Pattern(regexp = RegexUtil.REGEXP_NICKNAME, message = "invalid nickname format")
  String nickname,
  @NotBlank
  @Pattern(regexp = RegexUtil.REGEXP_MOBILE, message = "invalid phone number format")
  String mobile,
  @NotBlank
  String name,
  @NotBlank
  @Length(min = 8, max = VerifyUtil.MAX_DEFAULT_LENGTH)
  @Pattern(regexp = RegexUtil.REGEXP_PASSWORD, message = "invalid password format")
  String newPassword,
  @NotBlank
  @Length(min = 8, max = VerifyUtil.MAX_DEFAULT_LENGTH)
  @Pattern(regexp = RegexUtil.REGEXP_PASSWORD, message = "invalid password format")
  String confirmPassword
) {
}

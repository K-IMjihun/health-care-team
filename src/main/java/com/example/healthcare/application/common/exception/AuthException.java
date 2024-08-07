package com.example.healthcare.application.common.exception;

import com.example.healthcare.application.common.response.ResponseCode;
import lombok.AllArgsConstructor;

public class AuthException extends CommonException {

  @AllArgsConstructor
  public enum AuthExceptionCode implements ResponseCode {
    UNKNOWN_AUTH_ERROR("AEC-001", "unknown auth error"),
    JWT_REFRESH_TOKEN_VERIFICATION_FAIL("AEC-002", "Refresh token verification failed"),
    ALREADY_LOGOUT("AEC-003", "already logged out"),
    NOT_AUTHENTICATED("AEC-004", "not authenticated"),
    NOT_AUTHORIZED("AEC-005", "not authorized"),
    ;

    private final String code;
    private final String message;

    @Override
    public String getCode() {
      return code;
    }

    @Override
    public String getMessage() {
      return message;
    }
  }

  public AuthException(AuthExceptionCode code) {
    super(code);
  }

  public AuthException(AuthExceptionCode code, String message) {
    super(code, message);
  }

  public AuthException(String message) {
    super(AuthExceptionCode.UNKNOWN_AUTH_ERROR, message);
  }
}

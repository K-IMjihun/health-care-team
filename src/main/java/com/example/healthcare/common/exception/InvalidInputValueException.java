package com.example.healthcare.common.exception;

import com.example.healthcare.common.response.ResponseCode;

public class InvalidInputValueException extends CommonException {

  public enum InvalidInputValueExceptionCode implements ResponseCode {
    INVALID_INPUT_VALUE("IIV-001", "invalid input value"),
    METHOD_ARGUMENT_TYPE_MISMATCH("IIV-002", "method argument type mismatch"),
    ;

    private final String code;
    private final String message;

    InvalidInputValueExceptionCode(String code, String message) {
      this.code = code;
      this.message = message;
    }

    @Override
    public String getCode() {
      return code;
    }

    @Override
    public String getMessage() {
      return message;
    }
  }

  public InvalidInputValueException(InvalidInputValueExceptionCode code) {
    super(code);
  }

  public InvalidInputValueException(InvalidInputValueExceptionCode code, String detailMessage) {
    super(code, detailMessage);
  }

  public InvalidInputValueException(String detailMessage) {
    super(InvalidInputValueExceptionCode.INVALID_INPUT_VALUE, detailMessage);
  }
}

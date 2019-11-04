package com.g2.personalaccount.config;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.PRECONDITION_FAILED;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;

import com.g2.personalaccount.exceptions.ErrorResponse;
import com.g2.personalaccount.exceptions.InvalidArgumentsException;
import com.g2.personalaccount.exceptions.ResourceNotFoundException;
import java.time.format.DateTimeParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * @author Luis M. Kupferberg Ruiz (lkupferberg@overactive.com)
 * @created 2019-10-31 10:28
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
@ControllerAdvice("com.g2.personalaccount.controllers")
public class ControllerExceptionHandler extends ResponseEntityExceptionHandler {

  private static final Logger log = LoggerFactory.getLogger(ControllerExceptionHandler.class);

  private static final String MALFORMED_JSON_ERROR = "Malformed JSON request";
  private static final String DATETIME_PARSER_ERROR =
      "DateTime Parser error. DateTime format accepted: yyyy-MM-dd'T'HH:mm:ss.SSS";

  @ExceptionHandler(ResourceNotFoundException.class)
  public final ResponseEntity<Object> handleResourceNotFoundExceptions(Exception ex) {
    return generateErrorResponse(
        ex.getMessage(), PRECONDITION_FAILED, ((ResourceNotFoundException) ex).getTransactionId());
  }

  @ExceptionHandler(InvalidArgumentsException.class)
  public final ResponseEntity<Object> handleInvalidArgumentsExceptions(Exception ex) {
    return generateErrorResponse(
        ex.getMessage(), UNPROCESSABLE_ENTITY, ((InvalidArgumentsException) ex).getTransactionId());
  }

  @ExceptionHandler(Exception.class)
  public final ResponseEntity<Object> handleGenericExceptions(Exception ex) {
    log.error(ex.getMessage(), ex);
    return generateErrorResponse("HTTP 500 Internal Error", INTERNAL_SERVER_ERROR, null);
  }

  @Override
  protected ResponseEntity<Object> handleHttpMessageNotReadable(
      HttpMessageNotReadableException ex,
      HttpHeaders headers,
      HttpStatus status,
      WebRequest request) {
    String error = MALFORMED_JSON_ERROR;
    if ((ex.getMostSpecificCause() instanceof DateTimeParseException)) {
      error = DATETIME_PARSER_ERROR;
    }
    return generateErrorResponse(error, HttpStatus.BAD_REQUEST, null);
  }

  @Override
  protected ResponseEntity<Object> handleMethodArgumentNotValid(
      MethodArgumentNotValidException ex,
      HttpHeaders headers,
      HttpStatus status,
      WebRequest request) {

    BindingResult bindingResult = ex.getBindingResult();
    String errorMessage = bindingResult.getAllErrors().get(0).getDefaultMessage();

    return generateErrorResponse(errorMessage, status, null);
  }

  private ResponseEntity<Object> generateErrorResponse(
      String message, HttpStatus httpStatus, Long transactionId) {
    ErrorResponse error = new ErrorResponse();
    error.setTransactionId(transactionId);
    error.setMessage(message);
    error.setStatus(httpStatus.value());
    error.setError(httpStatus.getReasonPhrase());
    return new ResponseEntity(error, httpStatus);
  }
}

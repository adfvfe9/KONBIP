package com.konbip.global.error;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import com.konbip.global.response.ApiResponse;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(BusinessException.class)
	public ResponseEntity<ApiResponse<Void>> handleBusiness(BusinessException e) {
		return respond(e.getErrorCode(), e.getMessage());
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiResponse<Void>> handleValidation(MethodArgumentNotValidException e) {
		FieldError fieldError = e.getBindingResult().getFieldError();
		String message = fieldError == null
				? ErrorCode.INVALID_INPUT.getMessage()
				: fieldError.getField() + ": " + fieldError.getDefaultMessage();
		return respond(ErrorCode.INVALID_INPUT, message);
	}

	@ExceptionHandler(HandlerMethodValidationException.class)
	public ResponseEntity<ApiResponse<Void>> handleMethodValidation(HandlerMethodValidationException e) {
		String message = e.getAllErrors().stream()
				.findFirst()
				.map(error -> error.getDefaultMessage())
				.orElse(ErrorCode.INVALID_INPUT.getMessage());
		return respond(ErrorCode.INVALID_INPUT, message);
	}

	@ExceptionHandler({
			MissingServletRequestParameterException.class,
			MissingServletRequestPartException.class,
			MethodArgumentTypeMismatchException.class
	})
	public ResponseEntity<ApiResponse<Void>> handleBadRequest(Exception e) {
		return respond(ErrorCode.INVALID_INPUT, ErrorCode.INVALID_INPUT.getMessage() + " (" + e.getMessage() + ")");
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ApiResponse<Void>> handleNotReadable(HttpMessageNotReadableException e) {
		return respond(ErrorCode.INVALID_INPUT, "요청 본문(JSON) 형식이 올바르지 않습니다.");
	}

	@ExceptionHandler(HttpMediaTypeNotSupportedException.class)
	public ResponseEntity<ApiResponse<Void>> handleMediaType(HttpMediaTypeNotSupportedException e) {
		return respond(ErrorCode.INVALID_INPUT, "지원하지 않는 Content-Type 입니다. (" + e.getContentType() + ")");
	}

	@ExceptionHandler(MaxUploadSizeExceededException.class)
	public ResponseEntity<ApiResponse<Void>> handleUploadSize(MaxUploadSizeExceededException e) {
		return respond(ErrorCode.PAYLOAD_TOO_LARGE, ErrorCode.PAYLOAD_TOO_LARGE.getMessage());
	}

	@ExceptionHandler(HttpRequestMethodNotSupportedException.class)
	public ResponseEntity<ApiResponse<Void>> handleMethod(HttpRequestMethodNotSupportedException e) {
		return respond(ErrorCode.METHOD_NOT_ALLOWED, ErrorCode.METHOD_NOT_ALLOWED.getMessage());
	}

	@ExceptionHandler(NoResourceFoundException.class)
	public ResponseEntity<ApiResponse<Void>> handleNoResource(NoResourceFoundException e) {
		return respond(ErrorCode.NOT_FOUND, ErrorCode.NOT_FOUND.getMessage());
	}

	@ExceptionHandler(DataIntegrityViolationException.class)
	public ResponseEntity<ApiResponse<Void>> handleIntegrity(DataIntegrityViolationException e) {
		log.warn("Data integrity violation: {}", e.getMostSpecificCause().getMessage());
		return respond(ErrorCode.CONFLICT, ErrorCode.CONFLICT.getMessage());
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiResponse<Void>> handleUnexpected(Exception e) {
		log.error("Unhandled exception", e);
		return respond(ErrorCode.INTERNAL_ERROR, ErrorCode.INTERNAL_ERROR.getMessage());
	}

	private ResponseEntity<ApiResponse<Void>> respond(ErrorCode code, String message) {
		return ResponseEntity.status(code.getStatus()).body(ApiResponse.fail(code, message));
	}
}

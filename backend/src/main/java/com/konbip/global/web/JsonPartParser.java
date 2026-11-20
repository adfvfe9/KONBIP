package com.konbip.global.web;

import java.util.Comparator;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.konbip.global.error.BusinessException;
import com.konbip.global.error.ErrorCode;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

/**
 * multipart/form-data 의 JSON 파트(예: recipe)를 객체로 바꾸고 검증한다.
 * <p>
 * 프론트에서 FormData.append("recipe", JSON.stringify(...)) 처럼 문자열로 보내도,
 * new Blob([...], {type: "application/json"}) 으로 보내도 둘 다 받을 수 있게 문자열로 받아서 직접 파싱한다.
 */
@Component
@RequiredArgsConstructor
public class JsonPartParser {

	private final ObjectMapper objectMapper;
	private final Validator validator;

	public <T> T parse(String json, Class<T> type) {
		if (json == null || json.isBlank()) {
			throw new BusinessException(ErrorCode.INVALID_INPUT, "요청 데이터가 비어 있습니다.");
		}
		T value;
		try {
			value = objectMapper.readValue(json, type);
		} catch (JacksonException e) {
			throw new BusinessException(ErrorCode.INVALID_INPUT, "요청 JSON 형식이 올바르지 않습니다.");
		}
		validate(value);
		return value;
	}

	public <T> void validate(T value) {
		Set<ConstraintViolation<T>> violations = validator.validate(value);
		violations.stream()
				.min(Comparator.comparing(v -> v.getPropertyPath().toString()))
				.ifPresent(v -> {
					throw new BusinessException(ErrorCode.INVALID_INPUT, v.getPropertyPath() + ": " + v.getMessage());
				});
	}
}

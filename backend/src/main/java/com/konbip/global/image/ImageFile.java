package com.konbip.global.image;

import java.io.IOException;

import org.springframework.web.multipart.MultipartFile;

import com.konbip.global.error.BusinessException;
import com.konbip.global.error.ErrorCode;

/**
 * 검증을 마친 업로드 이미지. (FR-RCP-02: jpg·png, 5MB 이하)
 * Content-Type 헤더는 믿지 않고 파일 앞부분(매직 넘버)으로 형식을 판별한다.
 */
public record ImageFile(byte[] data, String contentType) {

	public static final long MAX_BYTES = 5L * 1024 * 1024;

	/** 파일이 없거나 비어 있으면 null. */
	public static ImageFile fromNullable(MultipartFile file) {
		if (file == null || file.isEmpty()) {
			return null;
		}
		return from(file);
	}

	public static ImageFile from(MultipartFile file) {
		if (file == null || file.isEmpty() || file.getSize() > MAX_BYTES) {
			throw new BusinessException(ErrorCode.INVALID_IMAGE);
		}
		byte[] bytes;
		try {
			bytes = file.getBytes();
		} catch (IOException e) {
			throw new BusinessException(ErrorCode.INVALID_IMAGE);
		}
		String type = detectType(bytes);
		if (type == null) {
			throw new BusinessException(ErrorCode.INVALID_IMAGE);
		}
		return new ImageFile(bytes, type);
	}

	private static String detectType(byte[] b) {
		if (b.length >= 3 && (b[0] & 0xFF) == 0xFF && (b[1] & 0xFF) == 0xD8 && (b[2] & 0xFF) == 0xFF) {
			return "image/jpeg";
		}
		if (b.length >= 8 && (b[0] & 0xFF) == 0x89 && b[1] == 'P' && b[2] == 'N' && b[3] == 'G'
				&& b[4] == 0x0D && b[5] == 0x0A && b[6] == 0x1A && b[7] == 0x0A) {
			return "image/png";
		}
		return null;
	}
}

package com.konbip.global.image;

import java.time.Duration;

import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

public record StoredImage(byte[] data, String contentType) {

	public ResponseEntity<byte[]> toResponse() {
		return ResponseEntity.ok()
				.contentType(MediaType.parseMediaType(contentType))
				.cacheControl(CacheControl.maxAge(Duration.ofMinutes(1)).cachePublic())
				.body(data);
	}
}

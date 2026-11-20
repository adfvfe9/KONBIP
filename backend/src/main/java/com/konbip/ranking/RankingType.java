package com.konbip.ranking;

import java.util.Locale;

import com.konbip.global.error.BusinessException;
import com.konbip.global.error.ErrorCode;

/** 랭킹 기준 (API 명세서 RANKING-001 type): views, bookmarks, rating */
public enum RankingType {
	VIEWS,
	BOOKMARKS,
	RATING;

	public static RankingType from(String value) {
		if (value == null || value.isBlank()) {
			return VIEWS;
		}
		try {
			return valueOf(value.trim().toUpperCase(Locale.ROOT));
		} catch (IllegalArgumentException e) {
			throw new BusinessException(ErrorCode.INVALID_INPUT, "type 은 views, bookmarks, rating 중 하나여야 합니다.");
		}
	}
}

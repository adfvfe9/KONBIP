package com.konbip.ranking;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Locale;

import com.konbip.global.error.BusinessException;
import com.konbip.global.error.ErrorCode;

/**
 * 랭킹 집계 기간 (API 명세서 RANKING-001 period): daily, weekly, monthly, all
 * 지금 시각 기준으로 최근 1일 / 7일 / 30일 동안의 활동을 센다. all 은 전체 기간.
 */
public enum RankingPeriod {
	DAILY(Duration.ofDays(1)),
	WEEKLY(Duration.ofDays(7)),
	MONTHLY(Duration.ofDays(30)),
	ALL(null);

	private final Duration window;

	RankingPeriod(Duration window) {
		this.window = window;
	}

	/** 집계 시작 시각. ALL 이면 null */
	public LocalDateTime since(LocalDateTime now) {
		return window == null ? null : now.minus(window);
	}

	public static RankingPeriod from(String value) {
		if (value == null || value.isBlank()) {
			return WEEKLY;
		}
		try {
			return valueOf(value.trim().toUpperCase(Locale.ROOT));
		} catch (IllegalArgumentException e) {
			throw new BusinessException(ErrorCode.INVALID_INPUT, "period 는 daily, weekly, monthly, all 중 하나여야 합니다.");
		}
	}
}

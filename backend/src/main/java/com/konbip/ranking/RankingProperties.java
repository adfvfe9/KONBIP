package com.konbip.ranking;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @param minReviewCount 별점 랭킹에 들어가기 위한 기간 내 최소 리뷰 수 (FR-RNK-02 안: 3개)
 */
@ConfigurationProperties("konbip.ranking")
public record RankingProperties(int minReviewCount) {
}

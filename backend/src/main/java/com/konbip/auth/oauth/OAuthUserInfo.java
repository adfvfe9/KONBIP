package com.konbip.auth.oauth;

/**
 * 소셜 제공자에게서 받은 회원 식별 정보.
 *
 * @param providerUserId 제공자 쪽 회원 ID (Google sub, Kakao id)
 * @param email          제공자가 인증한 이메일. 동의하지 않았거나 인증되지 않았으면 null
 */
public record OAuthUserInfo(String providerUserId, String email) {
}

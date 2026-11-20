package com.konbip.user;

import com.konbip.global.entity.BaseTimeEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "user_id")
	private Long id;

	@Column(length = 255)
	private String email;

	/** BCrypt 해시. 소셜 회원은 NULL */
	@Column(name = "password_hash")
	private String passwordHash;

	@Column(nullable = false, length = 50)
	private String nickname;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private Role role;

	@Enumerated(EnumType.STRING)
	@Column(length = 30)
	private AuthProvider provider;

	@Column(name = "provider_user_id")
	private String providerUserId;

	/** 프로필 사진 바이트는 ImageStore 가 SQL 로 직접 다룬다. 여기서는 존재 여부 확인용으로 읽기만 한다. */
	@Column(name = "profile_image_type", insertable = false, updatable = false)
	private String profileImageType;

	private User(String email, String passwordHash, String nickname, AuthProvider provider, String providerUserId) {
		this.email = email;
		this.passwordHash = passwordHash;
		this.nickname = nickname;
		this.role = Role.USER;
		this.provider = provider;
		this.providerUserId = providerUserId;
	}

	public static User emailUser(String email, String passwordHash, String nickname) {
		return new User(email, passwordHash, nickname, null, null);
	}

	public static User socialUser(AuthProvider provider, String providerUserId, String email, String nickname) {
		return new User(email, null, nickname, provider, providerUserId);
	}

	public boolean isSocialUser() {
		return provider != null;
	}

	public boolean hasProfileImage() {
		return profileImageType != null;
	}

	public void changeNickname(String nickname) {
		this.nickname = nickname;
	}

	public void changePassword(String passwordHash) {
		this.passwordHash = passwordHash;
	}

	public void grantRole(Role role) {
		this.role = role;
	}
}

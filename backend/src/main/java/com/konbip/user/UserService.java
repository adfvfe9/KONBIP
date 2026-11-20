package com.konbip.user;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.konbip.global.error.BusinessException;
import com.konbip.global.error.ErrorCode;
import com.konbip.global.image.ImageFile;
import com.konbip.global.image.ImageStore;
import com.konbip.global.image.StoredImage;
import com.konbip.recipe.RecipeRepository;
import com.konbip.report.ReportRepository;
import com.konbip.user.dto.ChangePasswordRequest;
import com.konbip.user.dto.UpdateProfileRequest;
import com.konbip.user.dto.UserResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

	private final UserRepository userRepository;
	private final RecipeRepository recipeRepository;
	private final ReportRepository reportRepository;
	private final PasswordEncoder passwordEncoder;
	private final ImageStore imageStore;

	/** AUTH-007 내 정보 */
	public UserResponse getMe(Long userId) {
		return UserResponse.from(find(userId));
	}

	/** AUTH-008 내 정보 수정 (닉네임). FR-MYP-01 */
	@Transactional
	public UserResponse updateProfile(Long userId, UpdateProfileRequest request) {
		User user = find(userId);
		String nickname = request.nickname().trim();
		if (!nickname.equals(user.getNickname()) && userRepository.existsByNickname(nickname)) {
			throw new BusinessException(ErrorCode.DUPLICATE_NICKNAME);
		}
		user.changeNickname(nickname);
		return UserResponse.from(user);
	}

	/** FR-AUT-06 비밀번호 변경 (이메일 가입 회원만) */
	@Transactional
	public void changePassword(Long userId, ChangePasswordRequest request) {
		User user = find(userId);
		if (user.isSocialUser() || user.getPasswordHash() == null) {
			throw new BusinessException(ErrorCode.SOCIAL_ACCOUNT_NO_PASSWORD);
		}
		if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
			throw new BusinessException(ErrorCode.PASSWORD_MISMATCH);
		}
		user.changePassword(passwordEncoder.encode(request.newPassword()));
	}

	/** FR-MYP-01 프로필 사진 변경 */
	@Transactional
	public UserResponse updateProfileImage(Long userId, ImageFile image) {
		User user = find(userId);
		imageStore.saveProfileImage(userId, image);
		return UserResponse.from(user, true);
	}

	@Transactional
	public UserResponse deleteProfileImage(Long userId) {
		User user = find(userId);
		imageStore.deleteProfileImage(userId);
		return UserResponse.from(user, false);
	}

	public StoredImage getProfileImage(Long userId) {
		return imageStore.findProfileImage(userId)
				.orElseThrow(() -> new BusinessException(ErrorCode.IMAGE_NOT_FOUND));
	}

	/**
	 * FR-AUT-07 회원 탈퇴. 요구사항 정의서 10-8 결정안대로 작성한 콘텐츠도 삭제한다.
	 * <ul>
	 *   <li>내 꿀조합 → 삭제 (그 꿀조합의 상품 구성·좋아요·댓글·리뷰·조회 이력도 DB CASCADE 로 삭제)</li>
	 *   <li>내가 한 신고 → 삭제</li>
	 *   <li>내 좋아요·즐겨찾기·댓글·리뷰 → users 삭제 시 CASCADE 로 삭제</li>
	 *   <li>내 조회 이력 → user_id 만 NULL (랭킹 집계는 유지)</li>
	 * </ul>
	 */
	@Transactional
	public void withdraw(Long userId) {
		find(userId);
		recipeRepository.deleteAllByUserId(userId);
		reportRepository.deleteAllByUserId(userId);
		userRepository.deleteById(userId);
	}

	private User find(Long userId) {
		return userRepository.findById(userId)
				.orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
	}
}

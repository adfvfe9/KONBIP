package com.konbip.comment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** FR-SOC-03 댓글: 최대 300자, 빈 내용 불가 */
public record CommentRequest(
		@NotBlank(message = "댓글 내용을 입력해 주세요.")
		@Size(max = 300, message = "댓글은 300자 이하여야 합니다.")
		String content
) {
}

package com.konbip.report;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReportRepository extends JpaRepository<Report, Long> {

	boolean existsByUserIdAndTargetTypeAndTargetId(Long userId, ReportTargetType targetType, Long targetId);

	Page<Report> findByStatus(ReportStatus status, Pageable pageable);

	/** 회원 탈퇴 시 그 회원이 한 신고를 지운다. (reports.user_id 는 ON DELETE CASCADE 가 없다) */
	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Query("DELETE FROM Report r WHERE r.userId = :userId")
	int deleteAllByUserId(@Param("userId") Long userId);
}

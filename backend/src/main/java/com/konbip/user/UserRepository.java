package com.konbip.user;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

	Optional<User> findByEmail(String email);

	Optional<User> findByProviderAndProviderUserId(AuthProvider provider, String providerUserId);

	boolean existsByEmail(String email);

	boolean existsByNickname(String nickname);
}

package com.seohamin.pomoland.domain.user.repository;

import com.seohamin.pomoland.domain.user.entity.UserOauth;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserOauthRepository extends JpaRepository<UserOauth, Long> {

    Optional<UserOauth> findByProviderAndProviderUserId(String provider, String providerUserId);
}
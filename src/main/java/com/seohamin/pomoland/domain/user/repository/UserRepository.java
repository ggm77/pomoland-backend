package com.seohamin.pomoland.domain.user.repository;

import com.seohamin.pomoland.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}

package com.seohamin.pomoland.domain.user.repository;

import com.seohamin.pomoland.domain.user.entity.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {
    List<User> findAllByOrderByPointDesc(final Pageable pageable);
}
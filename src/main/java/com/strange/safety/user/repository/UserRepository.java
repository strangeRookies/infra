package com.strange.safety.user.repository;

import com.strange.safety.user.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByEmail(String email);

    Optional<User> findByEmailAndActiveTrue(String email);

    Optional<User> findByUserIdAndActiveTrue(Long userId);
}

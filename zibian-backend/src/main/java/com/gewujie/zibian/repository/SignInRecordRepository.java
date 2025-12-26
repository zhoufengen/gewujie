package com.gewujie.zibian.repository;

import com.gewujie.zibian.entity.SignInRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SignInRecordRepository extends JpaRepository<SignInRecord, Long> {
    Optional<SignInRecord> findByUserIdAndSignInTimeBetween(Long userId, LocalDateTime startOfDay, LocalDateTime endOfDay);
    List<SignInRecord> findByUserId(Long userId);
}

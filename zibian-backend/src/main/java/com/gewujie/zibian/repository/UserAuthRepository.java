package com.gewujie.zibian.repository;

import com.gewujie.zibian.model.UserAuth;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserAuthRepository extends JpaRepository<UserAuth, Long> {
    Optional<UserAuth> findByIdentityTypeAndIdentifier(String identityType, String identifier);
}

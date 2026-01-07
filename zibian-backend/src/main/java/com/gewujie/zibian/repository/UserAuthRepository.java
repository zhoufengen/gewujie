package com.gewujie.zibian.repository;

import com.gewujie.zibian.model.UserAuth;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

import com.gewujie.zibian.model.IdentityType;

public interface UserAuthRepository extends JpaRepository<UserAuth, Long> {
    Optional<UserAuth> findByIdentityTypeAndIdentifier(IdentityType identityType, String identifier);
}

package com.gewujie.zibian.repository;

import com.gewujie.zibian.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
}

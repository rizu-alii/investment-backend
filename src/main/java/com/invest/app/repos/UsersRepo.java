package com.invest.app.repos;


import com.invest.app.entities.UsersEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UsersRepo extends JpaRepository<UsersEntity, Long> {
    UsersEntity findByUsername(String email);
}

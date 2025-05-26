package com.invest.app.repos;

import com.invest.app.entities.InvestmentStatus;
import com.invest.app.entities.UserActiveInvestment;
import com.invest.app.entities.UsersEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserActiveInvestmentRepo extends JpaRepository<UserActiveInvestment, Long> {

    // Find active investments for a specific user
    List<UserActiveInvestment> findByUsersEntityIdAndStatus(Long userId, InvestmentStatus status);

    // Find all active investments for a specific user
    List<UserActiveInvestment> findByUsersEntityId(Long userId);

    // Find active investments by investment ID
    List<UserActiveInvestment> findByInvestmentIdAndStatus(Long investmentId, InvestmentStatus status);
    List<UserActiveInvestment> findByUsersEntity(UsersEntity usersEntity);

    // Optional: You can create custom queries as needed.
}

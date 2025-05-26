package com.invest.app.repos;



import com.invest.app.entities.Transaction;
import com.invest.app.entities.UsersEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransactionRepo extends JpaRepository<Transaction, Long> {

    List<Transaction> findByUsersEntity_IdOrderByCreatedAtDesc(Long userId);

    List<Transaction> findByUsersEntity(UsersEntity usersEntity);
    List<Transaction> findByUserActiveInvestment_IdOrderByCreatedAtDesc(Long investmentId);
    List<Transaction> findTop10ByUsersEntity_IdOrderByCreatedAtDesc(Long userId);


}

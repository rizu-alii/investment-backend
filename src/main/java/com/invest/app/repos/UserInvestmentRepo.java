package com.invest.app.repos;

import com.invest.app.entities.UserInvestment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

@Service
public interface UserInvestmentRepo extends JpaRepository<UserInvestment, Long> {

    }

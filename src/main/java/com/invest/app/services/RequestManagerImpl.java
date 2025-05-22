package com.invest.app.services;

import com.invest.app.entities.UserInvestment;
import com.invest.app.repos.UserInvestmentRepo;
import com.invest.app.requests.CreateInvestmentRequest;
import com.invest.app.response.CreateInvestmentResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RequestManagerImpl implements RequestManager {

    @Autowired
    UserInvestmentRepo userInvestmentRepo;

    @Override
    public void createInvestment(CreateInvestmentRequest request) {

        UserInvestment userInvestment = new UserInvestment();
        userInvestment.setName(request.getName());
        userInvestment.setDescription(request.getDescription());
        userInvestment.setCategory(request.getCategory());
        userInvestment.setFundSize(request.getFundSize());
        userInvestment.setProjectedReturn(request.getProjectedReturn());
        userInvestment.setRiskLevel(request.getRiskLevel());
        try {
            userInvestmentRepo.save(userInvestment);
        } catch (Exception e) {
            throw new RuntimeException("error occured while creating investment " + e.getMessage());

        }
    }
}
package com.invest.app.services;

import com.invest.app.requests.CreateInvestmentRequest;
import com.invest.app.response.CreateInvestmentResponse;
import org.springframework.stereotype.Service;

@Service
public interface RequestManager {
    void createInvestment(CreateInvestmentRequest request);
}

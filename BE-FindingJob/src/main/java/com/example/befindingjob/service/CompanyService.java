package com.example.befindingjob.service;

import com.example.befindingjob.dto.employer.CompanyInfo;
import com.example.befindingjob.model.ApiResponse;

import java.util.List;

public interface CompanyService {
    ApiResponse<Void> addCompany(CompanyInfo companyInfo);
    ApiResponse<List<CompanyInfo>> getAllCompaniesByUserId(String token);
}

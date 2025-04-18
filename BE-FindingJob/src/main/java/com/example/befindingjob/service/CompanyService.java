package com.example.befindingjob.service;

import com.example.befindingjob.dto.CompanyDTO;
import com.example.befindingjob.entity.Company;
import com.example.befindingjob.model.ApiResponse;

import java.util.List;

public interface CompanyService {
    ApiResponse<Void> addCompany(CompanyDTO companyDTO);
    ApiResponse<Void> updateCompany(CompanyDTO companyDTO);
    ApiResponse<Void> deleteCompany(int companyId);
    ApiResponse<List<Company>> getAllCompaniesByUserId(String token);
}

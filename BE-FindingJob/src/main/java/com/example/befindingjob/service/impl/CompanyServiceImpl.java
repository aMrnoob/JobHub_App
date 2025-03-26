package com.example.befindingjob.service.impl;

import com.example.befindingjob.dto.employer.CompanyInfo;
import com.example.befindingjob.entity.Company;
import com.example.befindingjob.entity.User;
import com.example.befindingjob.model.ApiResponse;
import com.example.befindingjob.repository.CompanyRepository;
import com.example.befindingjob.repository.UserRepository;
import com.example.befindingjob.service.CompanyService;
import com.example.befindingjob.service.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CompanyServiceImpl implements CompanyService {

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    @Override
    public ApiResponse<Void> addCompany(CompanyInfo companyInfo) {

        Optional<User> userOptional = userRepository.findById(companyInfo.getUserInfo().getUserId());
        User user = userOptional.get();

        Company company = new Company();
        company.setCompanyName(companyInfo.getCompanyName());
        company.setUser(user);
        company.setAddress(companyInfo.getAddress());
        company.setLogoUrl(companyInfo.getLogoUrl());
        company.setWebsite(companyInfo.getWebsite());
        company.setDescription(companyInfo.getDescription());
        company.setCreatedAt(java.time.LocalDateTime.now());

        companyRepository.save(company);
        return new ApiResponse<>(true, "Add your company successfully", null);
    }

    @Override
    public ApiResponse<List<CompanyInfo>> getAllCompaniesByUserId(String token) {
        if (token == null || !token.startsWith("Bearer ")) {
            return new ApiResponse<>(false, "");
        }

        token = token.substring(7);

        if (!jwtService.isTokenValid(token)) {
            return new ApiResponse<>(false, "Invalid or expired token.");
        }

        Integer userId = jwtService.extractUserId(token);

        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            return new ApiResponse<>(false, "", null);
        }

        List<Company> companies = companyRepository.findByUserId(userId);

        List<CompanyInfo> companyInfoList = companies.stream().map(company -> new CompanyInfo(
                company.getCompanyId(),
                company.getCompanyName(),
                null,
                company.getAddress(),
                company.getLogoUrl(),
                company.getWebsite(),
                company.getDescription(),
                null,
                null
        )).toList();

        return new ApiResponse<>(true, "", companyInfoList);
    }
}

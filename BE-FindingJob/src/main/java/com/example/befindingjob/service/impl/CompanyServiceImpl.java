package com.example.befindingjob.service.impl;

import com.example.befindingjob.dto.CompanyDTO;
import com.example.befindingjob.entity.Company;
import com.example.befindingjob.entity.User;
import com.example.befindingjob.mapper.CompanyMapper;
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

    @Autowired
    private CompanyMapper companyMapper;

    @Override
    public ApiResponse<Void> addCompany(CompanyDTO companyDTO) {
        Optional<User> userOpt = userRepository.findById(companyDTO.getUserId());
        if (userOpt.isEmpty()) {
            return new ApiResponse<>(false, "", null);
        }

        try {
            Company company = companyMapper.CompanyDTOtoCompany(companyDTO);
            company.setUser(userOpt.get());
            company.setCompanyId(null);
            companyRepository.save(company);
            return new ApiResponse<>(true, "Company added successfully!");
        } catch (Exception e) {
            e.printStackTrace();
            return new ApiResponse<>(false, "", null);
        }
    }

    @Override
    public ApiResponse<Void> updateCompany(Company company) {
        Optional<Company> existingCompanyOpt = companyRepository.findById(company.getCompanyId());

        if (existingCompanyOpt.isEmpty()) {
            return new ApiResponse<>(false, "Company not found.");
        }

        Company existingCompany = existingCompanyOpt.get();

        if (company.getCompanyName() != null) existingCompany.setCompanyName(company.getCompanyName());
        if (company.getDescription() != null) existingCompany.setDescription(company.getDescription());
        if (company.getAddress() != null) existingCompany.setAddress(company.getAddress());
        if (company.getLogoUrl() != null) existingCompany.setLogoUrl(company.getLogoUrl());
        if (company.getWebsite() != null) existingCompany.setWebsite(company.getWebsite());

        companyRepository.save(existingCompany);
        return new ApiResponse<>(true, "Company updated successfully.");
    }

    @Override
    public ApiResponse<List<Company>> getAllCompaniesByUserId(String token) {
        if (!jwtService.isValidToken(token)) {
            return new ApiResponse<>(false, "Invalid or expired token.", null);
        }

        Integer userId = jwtService.extractUserId(token);
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            return new ApiResponse<>(false, "User not found.", null);
        }

        List<Company> company = companyRepository.findByUserId(userId);
        return new ApiResponse<>(true, "", company);
    }
}

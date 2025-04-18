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
            return new ApiResponse<>(false, "Company added failed", null);
        }
    }

    @Override
    public ApiResponse<Void> updateCompany(CompanyDTO companyDTO) {
        Optional<Company> existingCompanyOpt = companyRepository.findById(companyDTO.getCompanyId());

        if (existingCompanyOpt.isEmpty()) {
            return new ApiResponse<>(false, "");
        }

        Company existingCompany = existingCompanyOpt.get();

        if (companyDTO.getCompanyName() != null) existingCompany.setCompanyName(companyDTO.getCompanyName());
        if (companyDTO.getDescription() != null) existingCompany.setDescription(companyDTO.getDescription());
        if (companyDTO.getAddress() != null) existingCompany.setAddress(companyDTO.getAddress());
        if (companyDTO.getLogoUrl() != null) existingCompany.setLogoUrl(companyDTO.getLogoUrl());
        if (companyDTO.getWebsite() != null) existingCompany.setWebsite(companyDTO.getWebsite());

        companyRepository.save(existingCompany);
        return new ApiResponse<>(true, "Company updated successfully.");
    }

    @Override
    public ApiResponse<Void> deleteCompany(int companyId) {
        Optional<Company> companyOpt = companyRepository.findById(companyId);

        if (companyOpt.isEmpty()) {
            return new ApiResponse<>(false, "Company does not exist" + companyId, null);
        }

        companyRepository.deleteById(companyId);
        return new ApiResponse<>(true, "Delete company successfully", null);
    }

    @Override
    public ApiResponse<List<Company>> getAllCompaniesByUserId(String token) {
        if (!jwtService.isValidToken(token)) {
            return new ApiResponse<>(false, "", null);
        }

        Integer userId = jwtService.extractUserId(token);
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            return new ApiResponse<>(false, "", null);
        }

        List<Company> company = companyRepository.findByUserId(userId);
        return new ApiResponse<>(true, "", company);
    }
}

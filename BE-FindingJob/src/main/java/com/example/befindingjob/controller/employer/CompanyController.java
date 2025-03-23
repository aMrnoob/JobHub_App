package com.example.befindingjob.controller.employer;

import com.example.befindingjob.dto.employer.CompanyInfo;
import com.example.befindingjob.model.ApiResponse;
import com.example.befindingjob.service.CompanyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/company")
public class CompanyController {

    @Autowired
    private CompanyService companyService;

    @PostMapping("/add-company")
    public ApiResponse<Void> addCompany(@RequestBody CompanyInfo companyInfo) {
        return companyService.addCompany(companyInfo);
    }

    @PostMapping("/get-all-companies")
    public ApiResponse<List<CompanyInfo>> getAllCompanies(@RequestHeader("token") String token) {
        return companyService.getAllCompanies(token);
    }
}

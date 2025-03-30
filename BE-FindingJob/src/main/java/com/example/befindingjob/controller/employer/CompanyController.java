package com.example.befindingjob.controller.employer;

import com.example.befindingjob.dto.CompanyDTO;
import com.example.befindingjob.entity.Company;
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
    public ApiResponse<Void> addCompany(@RequestBody CompanyDTO companyDTO) {
        return companyService.addCompany(companyDTO);
    }

    @PostMapping("/get-all-companies-by-userId")
    public ApiResponse<List<Company>> getAllCompaniesByUserId(@RequestHeader("token") String token) {
        return companyService.getAllCompaniesByUserId(token);
    }

    @PostMapping("/update-company")
    public ApiResponse<Void> updateCompany(@RequestBody Company company) {
        return companyService.updateCompany(company);
    }
}

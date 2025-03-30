package com.example.befindingjob.dto;

import com.example.befindingjob.entity.Company;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ItemCompanyDTO {
    private Integer companyId;
    private String companyName;
    private String logoUrl;
    private String description;

    public ItemCompanyDTO(Company company) {
        this.companyId = company.getCompanyId();
        this.companyName = company.getCompanyName();
        this.logoUrl = company.getLogoUrl();
        this.description = company.getDescription();
    }
}

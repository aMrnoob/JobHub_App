package com.example.befindingjob.dto;

import com.example.befindingjob.entity.Company;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.checkerframework.checker.units.qual.N;

@Data
@AllArgsConstructor
@NoArgsConstructor
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

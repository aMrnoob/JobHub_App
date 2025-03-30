package com.example.befindingjob.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
@AllArgsConstructor
public class CompanyDTO {
    private int companyId;
    private String companyName;
    private String description;
    private String address;
    private String logoUrl;
    private String website;
    private int userId;
}

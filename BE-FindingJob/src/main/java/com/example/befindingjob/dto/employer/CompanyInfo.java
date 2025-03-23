package com.example.befindingjob.dto.employer;

import com.example.befindingjob.dto.admin.UserInfo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompanyInfo {
    private int companyId;
    private String companyName;
    private UserInfo userInfo;
    private String address;
    private String logoUrl;
    private String website;
    private String description;
    private String createdAt;
    private String updatedAt;
}


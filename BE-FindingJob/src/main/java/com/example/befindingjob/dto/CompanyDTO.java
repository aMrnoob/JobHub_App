package com.example.befindingjob.dto;

import lombok.*;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CompanyDTO {
    private int companyId;
    private String companyName;
    private String description;
    private String address;
    private String logoUrl;
    private String website;
    private int userId;
}

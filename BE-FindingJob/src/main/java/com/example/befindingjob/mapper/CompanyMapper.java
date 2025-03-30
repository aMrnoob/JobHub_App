package com.example.befindingjob.mapper;

import com.example.befindingjob.dto.CompanyDTO;
import com.example.befindingjob.entity.Company;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper(componentModel = "spring")
public interface CompanyMapper {
    @Mappings({
            @Mapping(target = "jobs", ignore = true),
    })
    Company CompanyDTOtoCompany(CompanyDTO companyDTO);
}

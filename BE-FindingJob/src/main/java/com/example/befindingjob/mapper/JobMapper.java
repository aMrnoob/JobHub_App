package com.example.befindingjob.mapper;

import com.example.befindingjob.dto.JobDTO;
import com.example.befindingjob.dto.SkillDTO;
import com.example.befindingjob.entity.Job;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import java.util.HashSet;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface JobMapper {
    @Mappings({
            @Mapping(target = "company", ignore = true),
            @Mapping(target = "description", source = "jobDTO.description"),
            @Mapping(target = "requiredSkills", ignore = true)
    })
    Job JobDTOtoJob(JobDTO jobDTO);
}

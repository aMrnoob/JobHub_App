package com.example.befindingjob.mapper;

import com.example.befindingjob.dto.UserDTO;
import com.example.befindingjob.entity.User;
import org.mapstruct.*;

import static org.mapstruct.NullValuePropertyMappingStrategy.IGNORE;

@Mapper(componentModel = "spring")
public interface UserMapper {
    @BeanMapping(nullValuePropertyMappingStrategy = IGNORE)
    @Mappings({
            @Mapping(target = "fullname", source = "fullName"),
            @Mapping(target = "dateOfBirth", ignore = true),
    })
    void updateUserFromDto(UserDTO userDTO, @MappingTarget User user);
}

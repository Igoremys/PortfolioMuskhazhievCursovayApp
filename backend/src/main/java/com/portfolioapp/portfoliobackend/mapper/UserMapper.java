package com.portfolioapp.portfoliobackend.mapper;

import com.portfolioapp.portfoliobackend.dto.UserDto;
import com.portfolioapp.portfoliobackend.entity.UserProfile;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "photos", ignore = true)
    UserProfile toEntity(UserDto dto);

    UserDto toDto(UserProfile entity);
}
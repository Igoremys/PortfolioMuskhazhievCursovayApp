package com.portfolioapp.portfoliobackend.mapper;

import com.portfolioapp.portfoliobackend.dto.PhotoDto;
import com.portfolioapp.portfoliobackend.entity.Photo;
import com.portfolioapp.portfoliobackend.entity.UserProfile;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface PhotoMapper {
    PhotoMapper INSTANCE = Mappers.getMapper(PhotoMapper.class);

    @Mapping(target = "userProfile", ignore = true)
    @Mapping(target = "likedByUsers", ignore = true)
    Photo toEntity(PhotoDto dto);

    default PhotoDto toDto(Photo entity, UserProfile currentUser) {
        String fullName = (entity.getUserProfile() != null) ? entity.getUserProfile().getFullName() : null;
        String avatarUrl = (entity.getUserProfile() != null) ? entity.getUserProfile().getAvatarUrl() : null;

        return PhotoDto.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .description(entity.getDescription())
                .imageUrl(entity.getImageUrl())
                .author(entity.getAuthor())
                .authorFullName(fullName)          
                .authorAvatarUrl(avatarUrl)       
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .likesCount(entity.getLikesCount())
                .likedByCurrentUser(entity.isLikedBy(currentUser))
                .build();
    }

    default PhotoDto toDto(Photo entity) {
        return toDto(entity, null);
    }

    void updateEntityFromDto(PhotoDto dto, @MappingTarget Photo entity);
}

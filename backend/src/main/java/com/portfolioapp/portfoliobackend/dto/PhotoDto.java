package com.portfolioapp.portfoliobackend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PhotoDto {
    private Long id;
    private String title;
    private String description;
    private String imageUrl;
    private String author; // email автора

    private String authorFullName;
    private String authorAvatarUrl;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Builder.Default
    private int likesCount = 0;
    @Builder.Default
    private boolean likedByCurrentUser = false;
}

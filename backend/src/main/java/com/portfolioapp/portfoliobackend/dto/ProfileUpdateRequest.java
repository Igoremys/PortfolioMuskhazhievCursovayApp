package com.portfolioapp.portfoliobackend.dto;

import lombok.Data;

@Data
public class ProfileUpdateRequest {
    private String fullName;
    private String avatarUrl;
    private String description;
}
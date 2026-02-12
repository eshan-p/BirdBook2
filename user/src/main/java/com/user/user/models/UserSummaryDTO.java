package com.user.user.models;

public record UserSummaryDTO(
        String id,
        String username,
        String firstName,
        String lastName,
        String location,
        String role,
        String profilePic,
        String[] friends,
        String[] posts,
        String[] groups
) {}

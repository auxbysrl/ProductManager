package com.auxby.productmanager.api.v1.user.model;

import lombok.Builder;

import java.util.Date;

@Builder
public record UserDetails(
        String lastName,
        String firstName,
        String userName,
        String avatarUrl,
        Date lastSeen,
        Integer rating) {
}

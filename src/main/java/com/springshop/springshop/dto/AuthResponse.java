package com.springshop.springshop.dto;

import com.springshop.springshop.entity.enums.Role;

public record AuthResponse(String token, String email, String fullName, Role role) {
}

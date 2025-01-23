package com.backend.demo.utils;

import com.backend.demo.dtos.User.UserResponseDTO;
import com.backend.demo.model.Role;
import com.backend.demo.model.User;

import java.util.stream.Collectors;

public class UserUtils {
    public static UserResponseDTO userToUserResponseDTO(User user) {
        return new UserResponseDTO(
                user.getUserId(),
                user.getUsername(),
                user.getRoles().stream().map((Role::getRoleName)).collect(Collectors.toList()),
                user.isVerified(),
                user.isActive());
    }
}

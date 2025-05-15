package com.backend.demo.utils;

import com.backend.demo.dtos.CollaboratorDTO;
import com.backend.demo.dtos.User.UserBasicDTO;
import com.backend.demo.dtos.User.UserIdDTO;
import com.backend.demo.dtos.User.UserResponseDTO;
import com.backend.demo.model.Role;
import com.backend.demo.model.User;

import java.util.List;
import java.util.stream.Collectors;

public class UserUtils {
    public static UserResponseDTO userToUserResponseDTO(User user,
                                                        List<CollaboratorDTO> collaborators) {
        return new UserResponseDTO(
                user.getUserId(),
                user.getUsername(),
                user.getRoles().stream().map((Role::getRoleName)).collect(Collectors.toList()),
                user.isVerified(),
                user.isActive(),
                collaborators);
    }

    public static UserBasicDTO userToUserBasicDTO(User user) {
        return new UserBasicDTO(
                user.getUserId(),
                user.getUsername(),
                user.getRoles().stream().map((Role::getRoleName)).collect(Collectors.toList()),
                user.isVerified(),
                user.isActive());
    }

    public static UserIdDTO userToUserIdDTO(User user) {
        return new UserIdDTO(
                user.getUserId(),
                user.getUsername());
    }
}

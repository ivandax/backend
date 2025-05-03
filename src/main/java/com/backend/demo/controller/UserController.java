package com.backend.demo.controller;

import com.backend.demo.config.CustomUserDetails;
import com.backend.demo.dtos.ResourceResponseDTO;
import com.backend.demo.dtos.User.UserBasicDTO;
import com.backend.demo.dtos.User.UserResponseDTO;
import com.backend.demo.service.CustomUserDetailsService;
import com.backend.demo.service.UserService;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {
    @Autowired
    private UserService userService;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @RequestMapping(value = "", method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    public ResourceResponseDTO<UserBasicDTO> listAllUsers(
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "perPage", required = false) Integer perPage,
            @RequestParam(value = "sortBy",required = false) String sortBy,
            @RequestParam(value = "sortDirection", required = false) Sort.Direction sortDirection) {
        return userService.findAll(page, perPage, sortBy, sortDirection);
    }

    @RequestMapping(value = "/logged-in-user", method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    public UserResponseDTO getLoggedInUser(
            @AuthenticationPrincipal CustomUserDetails userPrincipal) throws BadRequestException {
        return userService.findLoggedInUser(userPrincipal);
    }

}

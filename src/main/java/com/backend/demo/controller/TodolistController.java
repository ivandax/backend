package com.backend.demo.controller;

import com.backend.demo.config.CustomUserDetails;
import com.backend.demo.dtos.ResourceResponseDTO;
import com.backend.demo.dtos.TodolistDTO;
import com.backend.demo.dtos.TodolistRequestDTO;
import com.backend.demo.model.User;
import com.backend.demo.service.TodolistService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/todolists")
public class TodolistController {
    @Autowired
    private TodolistService todolistService;

    @RequestMapping(value = "/create", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.CREATED)
    public void createTodolist(
            @AuthenticationPrincipal CustomUserDetails userPrincipal,
            @RequestBody @Valid TodolistRequestDTO dto) {
        todolistService.createTodolist(userPrincipal.getUser(), dto);
    }

    @RequestMapping(value = "", method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    public ResourceResponseDTO<TodolistDTO> getTodolistForUser(
            @AuthenticationPrincipal CustomUserDetails userPrincipal,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "perPage", required = false) Integer perPage,
            @RequestParam(value = "sortBy", required = false) String sortBy,
            @RequestParam(value = "sortDirection", required = false) Sort.Direction sortDirection) {

        User user = userPrincipal.getUser();

        return todolistService.findAllForUser(user, page, perPage, sortBy, sortDirection);
    }

}

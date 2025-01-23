package com.backend.demo.controller;

import com.backend.demo.config.CustomUserDetails;
import com.backend.demo.dtos.ResourceResponseDTO;
import com.backend.demo.dtos.TodolistDTO;
import com.backend.demo.service.TodolistService;
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

    @PostMapping("/create")
    @ResponseStatus(HttpStatus.CREATED)
    public void createTodolist(
            @AuthenticationPrincipal CustomUserDetails userPrincipal) {
        System.out.println("machjie" + " " + userPrincipal);
        todolistService.createTodolist(userPrincipal.getUser());
    }

    @RequestMapping(value = "", method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    public ResourceResponseDTO<TodolistDTO> listAllUsers(
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "perPage", required = false) Integer perPage,
            @RequestParam(value = "sortBy", required = false) String sortBy,
            @RequestParam(value = "sortDirection", required = false) Sort.Direction sortDirection) {
        return todolistService.findAll(page, perPage, sortBy, sortDirection);
    }

}

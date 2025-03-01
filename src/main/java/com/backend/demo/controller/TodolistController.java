package com.backend.demo.controller;

import com.backend.demo.config.CustomUserDetails;
import com.backend.demo.dtos.*;
import com.backend.demo.model.User;
import com.backend.demo.service.TodolistService;
import jakarta.validation.Valid;
import org.apache.coyote.BadRequestException;
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
            @RequestBody @Valid TodolistCreateRequestDTO dto) {
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

    @RequestMapping(value = "/{id}/update", method = RequestMethod.PATCH)
    @ResponseStatus(HttpStatus.OK)
    public void updateTodolist(
            @PathVariable Integer id,
            @AuthenticationPrincipal CustomUserDetails userPrincipal,
            @RequestBody @Valid TodolistUpdateRequestDTO dto) throws BadRequestException {
        todolistService.updateTodolist(id, dto, userPrincipal);
    }

    @RequestMapping(value = "/{id}/add-todo", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.CREATED)
    public void addTodo(
            @PathVariable Integer id,
            @AuthenticationPrincipal CustomUserDetails userPrincipal,
            @RequestBody @Valid TodoRequestDTO dto) throws BadRequestException {
        todolistService.addTodo(id, dto, userPrincipal);
    }

    @RequestMapping(value = "/{id}/todos/{todoId}", method = RequestMethod.PATCH)
    @ResponseStatus(HttpStatus.OK)
    public void updateTodo(
            @PathVariable Integer id,
            @PathVariable Integer todoId,
            @AuthenticationPrincipal CustomUserDetails userPrincipal,
            @RequestBody @Valid TodoRequestDTO dto) throws BadRequestException {
        todolistService.updateTodo(id, todoId, dto, userPrincipal);
    }

    @RequestMapping(value = "/{id}/delete", method = RequestMethod.DELETE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTodolist(
            @PathVariable Integer id,
            @AuthenticationPrincipal CustomUserDetails userPrincipal) throws BadRequestException {
        todolistService.deleteTodolist(id, userPrincipal);
    }

}

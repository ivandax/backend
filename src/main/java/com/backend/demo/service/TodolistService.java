package com.backend.demo.service;

import com.backend.demo.config.ConfigProperties;
import com.backend.demo.config.CustomUserDetails;
import com.backend.demo.dtos.*;
import com.backend.demo.model.Todo;
import com.backend.demo.model.Todolist;
import com.backend.demo.model.User;
import com.backend.demo.repository.*;
import com.backend.demo.utils.PaginationUtils;
import com.backend.demo.utils.TodolistUtils;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TodolistService {

    @Autowired
    private TodolistRepository todolistRepository;

    @Autowired
    private TodoRepository todoRepository;

    @Autowired
    private ConfigProperties configProperties;

    public void createTodolist(User user, TodolistRequestDTO todolistRequestDTO) {
        Todolist newTodoList = new Todolist(user);
        newTodoList.setTitle(todolistRequestDTO.getTitle());
        newTodoList.setDescription(todolistRequestDTO.getDescription());
        List<Todo> todos = todolistRequestDTO.getTodos().stream()
                .map(todoRequestDTO -> new Todo(todoRequestDTO.getDescription(), newTodoList)).toList();
        newTodoList.setTodos(todos);

        todolistRepository.save(newTodoList);
    }

    public ResourceResponseDTO<TodolistDTO> findAllForUser(User user, Integer page, Integer perPage,
                                                           String sortBy,
                                                           Sort.Direction sortDirection) {
        Pageable paginationConfig = PaginationUtils.getPaginationConfig(page, perPage, sortBy,
                sortDirection);
        Page<Todolist> todolists = todolistRepository.findByCreatedByOrSharedWith(user,
                paginationConfig);
        return new ResourceResponseDTO<>(
                todolists.stream().map(TodolistUtils::toDTO).collect(Collectors.toList()),
                todolists.getTotalPages(),
                PaginationUtils.getPage(page),
                PaginationUtils.getPerPage(perPage)
        );
    }

    public void updateTodolist(Integer id, TodolistUpdateRequestDTO dto,
                               CustomUserDetails userDetails) throws BadRequestException {
        Todolist todolist = todolistRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("Todolist not found"));

        boolean canEdit = checkCanEdit(todolist, userDetails);

        if (!canEdit) {
            throw new BadRequestException("Error of ownership. Does not belong to this todo list");
        }

        todolist.setTitle(dto.getTitle());
        todolist.setDescription(dto.getDescription());

        todolistRepository.save(todolist);
    }

    public void addTodo(Integer id, TodoRequestDTO dto, CustomUserDetails userDetails) throws BadRequestException {
        Todolist todolist = todolistRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("Todolist not found"));
        boolean canEdit = checkCanEdit(todolist, userDetails);

        if (!canEdit) {
            throw new BadRequestException("Error of ownership. Does not belong to this todo list");
        }

        Todo todo = new Todo(dto.getDescription(), todolist);
        todoRepository.save(todo);
    }

    public void updateTodo(Integer id, Integer todoId, TodoRequestDTO dto, CustomUserDetails userDetails) throws BadRequestException {
        Todolist todolist = todolistRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("Todolist not found"));
        boolean canEdit = checkCanEdit(todolist, userDetails);

        if (!canEdit) {
            throw new BadRequestException("Error of ownership. Does not belong to this todo list");
        }

        Todo todo = todoRepository.findById(todoId).orElseThrow(() -> new BadRequestException("Todo not found"));
        todo.setDescription(dto.getDescription());
        todo.setCompleted(dto.isCompleted());

        todoRepository.save(todo);
    }

    private boolean checkCanEdit(Todolist todolist, CustomUserDetails userDetails) {
        boolean isOwner = Objects.equals(todolist.getCreatedBy().getUserId(),
                userDetails.getUser().getUserId());

        boolean isSharedWith = todolist.getSharedWith().stream()
                .anyMatch(user -> Objects.equals(user.getUserId(),
                        userDetails.getUser().getUserId()));

        return isOwner || isSharedWith;
    }
}

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
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class TodolistService {

    @Autowired
    private TodolistRepository todolistRepository;

    @Autowired
    private TodoRepository todoRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ConfigProperties configProperties;

    public void createTodolist(User user, TodolistCreateRequestDTO todolistRequestDTO) {
        Todolist newTodoList = new Todolist(user);
        newTodoList.setTitle(todolistRequestDTO.getTitle());
        newTodoList.setDescription(todolistRequestDTO.getDescription());
        List<Todo> todos = todolistRequestDTO.getTodos().stream()
                .map(todoRequestDTO -> new Todo(todoRequestDTO.getDescription(), newTodoList,
                        todoRequestDTO.getSequenceNumber())).toList();
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

        Map<Integer, Todo> persistedTodos = todolist.getTodos().stream()
                .collect(Collectors.toMap(Todo::getId, todo -> todo));

        for (TodoUpdateDTO todoDTO : dto.getUpdateTodos()) {
            Todo existingTodo = persistedTodos.get(todoDTO.getId());
            if (existingTodo != null) {
                existingTodo.setDescription(todoDTO.getDescription());
                existingTodo.setCompleted(todoDTO.isCompleted());
            }
        }

        for (TodoRequestDTO newTodoDTO : dto.getCreateTodos()) {
            Todo newTodo = new Todo(newTodoDTO.getDescription(), todolist,
                    newTodoDTO.getSequenceNumber());
            todolist.addTodo(newTodo);
        }

        Set<Integer> deleteIds = dto.getDeleteTodoIds().stream()
                .map(Integer::parseInt)
                .collect(Collectors.toSet());

        todolist.getTodos().removeIf(todo -> deleteIds.contains(todo.getId()));

        todolistRepository.save(todolist);
    }

    public void addTodo(Integer id, TodoRequestDTO dto, CustomUserDetails userDetails) throws BadRequestException {
        Todolist todolist = todolistRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("Todolist not found"));
        boolean canEdit = checkCanEdit(todolist, userDetails);

        if (!canEdit) {
            throw new BadRequestException("Error of ownership. Does not belong to this todo list");
        }

        Todo todo = new Todo(dto.getDescription(), todolist, dto.getSequenceNumber());
        todoRepository.save(todo);
    }

    public void updateTodo(Integer id, Integer todoId, TodoRequestDTO dto,
                           CustomUserDetails userDetails) throws BadRequestException {
        Todolist todolist = todolistRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("Todolist not found"));
        boolean canEdit = checkCanEdit(todolist, userDetails);

        if (!canEdit) {
            throw new BadRequestException("Error of ownership. Does not belong to this todo list");
        }

        Todo todo = todoRepository.findById(todoId).orElseThrow(() -> new BadRequestException(
                "Todo not found"));
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

    public void deleteTodolist(Integer id, CustomUserDetails userDetails) throws BadRequestException {
        Todolist todolist = todolistRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("Todolist not found"));

        boolean canEdit = checkCanEdit(todolist, userDetails);

        if (!canEdit) {
            throw new BadRequestException("Error of ownership. You don't have permission to " +
                    "delete this todolist");
        }

        todolistRepository.delete(todolist);
    }

    public void addCollaboratorToTodolist(Integer todolistId, Integer collaboratorId,
                                          CustomUserDetails userDetails) throws BadRequestException {
        Todolist todolist = todolistRepository.findById(todolistId)
                .orElseThrow(() -> new BadRequestException("Todolist not found"));

        if (!checkCanEdit(todolist, userDetails)) {
            throw new BadRequestException("You don't have permission to share this todolist");
        }

        User collaborator = userRepository.findById(collaboratorId)
                .orElseThrow(() -> new BadRequestException("User to add not found"));

        if (collaborator.getUserId().equals(userDetails.getUser().getUserId())) {
            throw new BadRequestException("You cannot add yourself as a collaborator");
        }

        if (todolist.getSharedWith().contains(collaborator)) {
            throw new BadRequestException("This user is already a collaborator");
        }

        todolist.getSharedWith().add(collaborator);
        todolistRepository.save(todolist);
    }
}

package com.backend.demo.service;

import com.backend.demo.config.ConfigProperties;
import com.backend.demo.dtos.ResourceResponseDTO;
import com.backend.demo.dtos.TodolistDTO;
import com.backend.demo.model.Todolist;
import com.backend.demo.model.User;
import com.backend.demo.repository.*;
import com.backend.demo.utils.PaginationUtils;
import com.backend.demo.utils.TodolistUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
public class TodolistService {

    @Autowired
    private TodolistRepository todolistRepository;

    @Autowired
    private ConfigProperties configProperties;

    public void createTodolist(User user) {
        Todolist newTodoList = new Todolist(user);
        todolistRepository.save(newTodoList);
    }

    public ResourceResponseDTO<TodolistDTO> findAll(Integer page, Integer perPage,
                                                    String sortBy,
                                                    Sort.Direction sortDirection) {
        Pageable paginationConfig = PaginationUtils.getPaginationConfig(page, perPage, sortBy,
                sortDirection);
        Page<Todolist> todolists = todolistRepository.findAll(paginationConfig);
        return new ResourceResponseDTO<>(
                todolists.stream().map(TodolistUtils::toDTO).collect(Collectors.toList()),
                todolists.getTotalPages(),
                PaginationUtils.getPage(page),
                PaginationUtils.getPerPage(perPage)
        );
    }
}

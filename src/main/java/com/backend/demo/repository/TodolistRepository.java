package com.backend.demo.repository;

import com.backend.demo.model.Todolist;
import com.backend.demo.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface TodolistRepository extends JpaRepository<Todolist, Integer> {
    Optional<Todolist> findByTitle(String title);
    @Query("SELECT t FROM Todolist t WHERE t.createdBy = :user OR :user MEMBER OF t.sharedWith")
    Page<Todolist> findByCreatedByOrSharedWith(@Param("user") User user, Pageable pageable);

    @Query("SELECT t FROM Todolist t LEFT JOIN FETCH t.todos WHERE t.id = :id")
    Optional<Todolist> findByIdWithTodos(@Param("id") Integer id);
}
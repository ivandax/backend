package com.backend.demo.repository;

import com.backend.demo.model.Todo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TodoRepository extends JpaRepository<Todo, Integer> {
    Optional<Todo> findByDescription(String title);
}
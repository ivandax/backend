package com.backend.demo.repository;

import com.backend.demo.model.Todolist;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TodolistRepository extends JpaRepository<Todolist, Integer> {
}
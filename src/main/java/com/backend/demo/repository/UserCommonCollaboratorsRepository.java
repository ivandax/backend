package com.backend.demo.repository;

import com.backend.demo.model.User;
import com.backend.demo.model.UserCommonCollaborators;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface UserCommonCollaboratorsRepository extends JpaRepository<UserCommonCollaborators, Integer> {
    List<UserCommonCollaborators> findByUser(User user);

    boolean existsByUserAndCollaborator(User user, User collaborator);
}

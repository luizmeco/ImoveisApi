package com.example.projeto.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.projeto.models.UserModel;

public interface UserRepository extends JpaRepository<UserModel, Integer> {
    java.util.Optional<UserModel> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);
}
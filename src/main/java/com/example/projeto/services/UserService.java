package com.example.projeto.services;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.projeto.models.UserModel;
import com.example.projeto.repositories.UserRepository;

@Service 
public class UserService {

    @Autowired 
    private UserRepository userRepository;

    public List<UserModel> getAllUsers() {
        List<UserModel> users = userRepository.findAll();
        return users;
    }

    public UserModel find(Integer id) {
        Optional<UserModel> user = userRepository.findById(id);
        return user.orElse(null);
    }

    public UserModel insert(UserModel user) {
        return userRepository.save(user);
    }

    public UserModel update(UserModel user) {
        UserModel existingUser = this.find(user.getId());
        if (existingUser == null) {
            return null;
        }

        if(user.getNome() != null) {
            existingUser.setNome(user.getNome());
        }
        if(user.getEmail() != null) {
            existingUser.setEmail(user.getEmail());
        }
        if(user.getSenha() != null) {
            existingUser.setSenha(user.getSenha());
        }
        if(user.getTipo() != null) {
            existingUser.setTipo(user.getTipo());
        }

        return userRepository.save(existingUser);
    }

    public UserModel delete(Integer id) {
        UserModel user = this.find(id);
        if (user == null) {
            return null;
        }

        userRepository.deleteById(id);
        return user;
    }

}

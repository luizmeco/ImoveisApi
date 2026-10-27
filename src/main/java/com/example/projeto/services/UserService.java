package com.example.projeto.services;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import com.example.projeto.models.UserModel;
import com.example.projeto.models.UserRole;
import com.example.projeto.repositories.UserRepository;

@Service 
public class UserService {

    @Autowired 
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public List<UserModel> getAllUsers() {
        List<UserModel> users = userRepository.findAll();
        return users;
    }

    public UserModel find(Integer id) {
        Optional<UserModel> user = userRepository.findById(id);
        return user.orElse(null);
    }

    public UserModel insert(UserModel user) {
        validateUser(user);
        if (userRepository.existsByEmailIgnoreCase(user.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "E-mail já cadastrado.");
        }
        user.setTipo(normalizeRole(user.getTipo()));
        user.setSenha(passwordEncoder.encode(user.getSenha()));
        return userRepository.save(user);
    }

    public UserModel register(String nome, String email, String senha, String role) {
        UserModel user = new UserModel(null, nome, email, senha, role);
        return insert(user);
    }

    public UserModel findByEmail(String email) {
        return userRepository.findByEmailIgnoreCase(email).orElse(null);
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
            existingUser.setSenha(passwordEncoder.encode(user.getSenha()));
        }
        if(user.getTipo() != null) {
            existingUser.setTipo(normalizeRole(user.getTipo()));
        }
        if (user.getEmail() != null
                && !user.getEmail().equalsIgnoreCase(existingUser.getEmail())
                && userRepository.existsByEmailIgnoreCase(user.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "E-mail já cadastrado.");
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

    private void validateUser(UserModel user) {
        if (user == null || !StringUtils.hasText(user.getNome())
                || !StringUtils.hasText(user.getEmail()) || !StringUtils.hasText(user.getSenha())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Nome, e-mail e senha são obrigatórios.");
        }
        if (!StringUtils.hasText(user.getTipo())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Perfil de usuário obrigatório.");
        }
        normalizeRole(user.getTipo());
    }

    private String normalizeRole(String role) {
        try {
            return UserRole.fromValue(role).name();
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exception.getMessage(), exception);
        }
    }

}

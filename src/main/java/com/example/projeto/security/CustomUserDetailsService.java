package com.example.projeto.security;

import java.util.List;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UserDetailsPasswordService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import com.example.projeto.models.UserModel;
import com.example.projeto.models.UserRole;
import com.example.projeto.repositories.UserRepository;

@Service
public class CustomUserDetailsService implements UserDetailsService, UserDetailsPasswordService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        UserModel user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado."));
        try {
            return new User(user.getEmail(), user.getSenha(),
                    List.of(new SimpleGrantedAuthority(UserRole.fromValue(user.getTipo()).authority())));
        } catch (IllegalArgumentException exception) {
            throw new UsernameNotFoundException("Perfil do usuário inválido.", exception);
        }
    }

    @Override
    public UserDetails updatePassword(UserDetails user, String newPassword) {
        UserModel model = userRepository.findByEmailIgnoreCase(user.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado."));
        model.setSenha(newPassword);
        userRepository.save(model);
        return loadUserByUsername(user.getUsername());
    }
}

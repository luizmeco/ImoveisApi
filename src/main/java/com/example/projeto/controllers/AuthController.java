package com.example.projeto.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.projeto.dtos.AuthResponse;
import com.example.projeto.dtos.LoginRequest;
import com.example.projeto.dtos.RegisterRequest;
import com.example.projeto.models.UserModel;
import com.example.projeto.security.JwtService;
import com.example.projeto.services.UserService;

@RestController
@RequestMapping("/public")
public class AuthController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthController(
            UserService userService,
            AuthenticationManager authenticationManager,
            JwtService jwtService) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @PostMapping("/register")
    public ResponseEntity<Void> register(@RequestBody RegisterRequest request) {
        if (request == null || !StringUtils.hasText(request.nome())
                || !StringUtils.hasText(request.email()) || !StringUtils.hasText(request.password())
                || !StringUtils.hasText(request.role())) {
            return ResponseEntity.badRequest().build();
        }
        userService.register(request.nome(), request.email(), request.password(), request.role());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        if (request == null || !StringUtils.hasText(request.email())
                || !StringUtils.hasText(request.password())) {
            return ResponseEntity.badRequest().build();
        }
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email(), request.password()));
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            UserModel user = userService.findByEmail(userDetails.getUsername());
            if (user == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            return ResponseEntity.ok(new AuthResponse(jwtService.generateToken(userDetails)));
        } catch (BadCredentialsException exception) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }
}

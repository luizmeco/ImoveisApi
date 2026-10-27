package com.example.projeto.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.example.projeto.dtos.LoginRequest;
import com.example.projeto.dtos.RegisterRequest;
import com.example.projeto.models.UserModel;
import com.example.projeto.security.JwtService;
import com.example.projeto.services.UserService;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthControllerTests {

    private final UserService userService = mock(UserService.class);
    private final AuthenticationManager authenticationManager = mock(AuthenticationManager.class);
    private final JwtService jwtService = mock(JwtService.class);
    private final AuthController controller =
            new AuthController(userService, authenticationManager, jwtService);

    @Test
    void registerAcceptsRequestedRoleAndReturnsEmptyOkResponse() {
        var response = controller.register(new RegisterRequest(
                "Teste", "teste@example.com", "123", "ADMIN"));

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNull(response.getBody());
        verify(userService).register("Teste", "teste@example.com", "123", "ADMIN");
    }

    @Test
    void loginReturnsOnlyTheToken() {
        Authentication authentication = mock(Authentication.class);
        var userDetails = User.withUsername("teste@example.com")
                .password("encoded")
                .authorities("ROLE_ADMIN")
                .build();
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userService.findByEmail("teste@example.com"))
                .thenReturn(new UserModel(1, "Teste", "teste@example.com", "encoded", "ADMINISTRADOR"));
        when(jwtService.generateToken(userDetails)).thenReturn("jwt-token");

        var response = controller.login(new LoginRequest("teste@example.com", "123"));

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("jwt-token", response.getBody().token());
    }

    @Test
    void loginReturnsUnauthorizedForInvalidCredentials() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Credenciais inválidas."));

        var response = controller.login(new LoginRequest("teste@example.com", "incorreta"));

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void endpointsAcceptEnglishJsonFieldsAndReturnExpectedBodies() throws Exception {
        Authentication authentication = mock(Authentication.class);
        var userDetails = User.withUsername("teste@example.com")
                .password("encoded")
                .authorities("ROLE_ADMIN")
                .build();
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userService.findByEmail("teste@example.com"))
                .thenReturn(new UserModel(1, "Teste", "teste@example.com", "encoded", "ADMINISTRADOR"));
        when(jwtService.generateToken(userDetails)).thenReturn("jwt-token");
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        mockMvc.perform(post("/public/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome":"Teste","email":"teste@example.com","password":123,"role":"ADMIN"}
                                """))
                .andExpect(status().isOk())
                .andExpect(content().string(""));
        verify(userService).register("Teste", "teste@example.com", "123", "ADMIN");

        mockMvc.perform(post("/public/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"teste@example.com","password":"123"}
                                """))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"token\":\"jwt-token\"}", true));
        verify(authenticationManager).authenticate(argThat(token ->
                token.getPrincipal().equals("teste@example.com")
                        && token.getCredentials().equals("123")));
    }
}

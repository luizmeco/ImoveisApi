package com.example.projeto.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

    @Bean
    SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtAuthenticationFilter jwtAuthenticationFilter,
            AuthenticationProvider authenticationProvider) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(errors -> errors
                        .authenticationEntryPoint((request, response, exception) ->
                                response.sendError(401, "Unauthorized"))
                        .accessDeniedHandler((request, response, exception) ->
                                response.sendError(403, "Forbidden")))
                .authenticationProvider(authenticationProvider)
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/public/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/imoveis", "/imoveis/*").permitAll()
                        .requestMatchers(HttpMethod.PATCH, "/imoveis/*/status")
                                .hasAnyRole("ADMIN", "CORRETOR")
                        .requestMatchers(HttpMethod.POST, "/imoveis").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/imoveis/*").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/imoveis/*").hasRole("ADMIN")
                        .requestMatchers("/users/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/bairros/**", "/tipos-imoveis/**", "/fotos/**")
                                .hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/bairros/**", "/tipos-imoveis/**", "/fotos/**")
                                .hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/bairros/**", "/tipos-imoveis/**", "/fotos/**")
                                .hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/bairros/**", "/tipos-imoveis/**", "/fotos/**")
                                .hasRole("ADMIN")
                        .anyRequest().authenticated())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new LegacyAwarePasswordEncoder();
    }

    @Bean
    AuthenticationProvider authenticationProvider(
            CustomUserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        provider.setUserDetailsPasswordService(userDetailsService);
        return provider;
    }

    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }
}

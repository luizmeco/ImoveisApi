package com.example.projeto.security;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.cors.CorsConfiguration;

class SecurityConfigurationTests {

    @Test
    void jwtFilterIsRegisteredOnlyInTheSpringSecurityChain() {
        var filter = new JwtAuthenticationFilter(null, null);
        FilterRegistrationBean<JwtAuthenticationFilter> registration =
                new SecurityConfiguration().jwtAuthenticationFilterRegistration(filter);

        assertFalse(registration.isEnabled());
    }

    @Test
    void accessDeniedSets403WithoutStartingAnErrorDispatch() throws Exception {
        var response = mock(MockHttpServletResponse.class);

        new SecurityConfiguration().accessDeniedHandler()
                .handle(new MockHttpServletRequest(), response, new AccessDeniedException("Denied"));

        verify(response).setStatus(403);
        verify(response, never()).sendError(403);
    }

    @Test
    void corsAllowsFrontendMutationMethods() {
        var source = new SecurityConfiguration()
                .corsConfigurationSource("http://localhost:5173");
        CorsConfiguration cors = source.getCorsConfiguration(
                new MockHttpServletRequest("OPTIONS", "/bairros"));

        assertNotNull(cors);
        assertTrue(cors.getAllowedMethods().containsAll(
                java.util.List.of("POST", "PUT", "PATCH", "DELETE", "OPTIONS")));
        assertTrue(cors.getAllowedHeaders().containsAll(
                java.util.List.of("Authorization", "Content-Type")));
    }
}

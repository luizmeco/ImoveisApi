package com.example.projeto.models;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class UserRoleTests {

    @Test
    void mapsAdminAliasesToAdminAuthority() {
        assertEquals(UserRole.ADMINISTRADOR, UserRole.fromValue("ADMIN"));
        assertEquals("ROLE_ADMIN", UserRole.fromValue("ADMINISTRADOR").authority());
    }

    @Test
    void mapsClientAliasesToUserAuthority() {
        assertEquals(UserRole.USER, UserRole.fromValue("USER"));
        assertEquals("ROLE_USER", UserRole.fromValue("CLIENTE").authority());
    }

    @Test
    void rejectsUnsupportedProfiles() {
        assertThrows(IllegalArgumentException.class, () -> UserRole.fromValue("ROOT"));
    }
}

package com.example.projeto.security;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class LegacyAwarePasswordEncoderTests {

    private final LegacyAwarePasswordEncoder passwordEncoder = new LegacyAwarePasswordEncoder();

    @Test
    void upgradesLegacyPlainTextPasswordsToBcrypt() {
        assertTrue(passwordEncoder.matches("senha-legada", "senha-legada"));
        assertTrue(passwordEncoder.upgradeEncoding("senha-legada"));

        String encoded = passwordEncoder.encode("senha-legada");
        assertTrue(passwordEncoder.matches("senha-legada", encoded));
        assertFalse(passwordEncoder.upgradeEncoding(encoded));
    }
}

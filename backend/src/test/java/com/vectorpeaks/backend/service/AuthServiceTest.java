/*
 * AuthServiceTest.java
 *
 * Version: 1.1
 * Date: 2026-04-20
 *
 * Copyright (c) 2026 EduLink Team. All rights reserved.
 *
 * This software is the confidential and proprietary information of EduLink.
 */

package com.vectorpeaks.backend.service;

import com.vectorpeaks.backend.entity.User;
import com.vectorpeaks.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link AuthService}.
 *
 * <p>Weryfikuje logikę uwierzytelniania użytkowników, w tym:
 * <ul>
 *   <li>poprawne logowanie przy prawidłowych danych,</li>
 *   <li>odrzucenie przy błędnym e-mailu lub haśle,</li>
 *   <li>obsługę pustych wartości wejściowych,</li>
 *   <li>poprawność interakcji z {@link UserRepository}.</li>
 * </ul>
 *
 * <p>Używa Mockito ({@code @ExtendWith(MockitoExtension.class)}) –
 * nie uruchamia kontekstu Springa ani bazy danych.
 *
 * @version 1.1
 * @author EduLink Team
 * @see AuthService
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    /** Mock repozytorium użytkowników – zastępuje warstwę bazodanową. */
    @Mock
    private UserRepository userRepository;

    /**
     * Testowany obiekt z automatycznie wstrzykniętym mockiem
     * {@link UserRepository}.
     */
    @InjectMocks
    private AuthService authService;

    /** Pomocniczy enkoder używany do tworzenia zahashowanych haseł w testach. */
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    /** Przykładowy użytkownik inicjalizowany przed każdym testem. */
    private User mockUser;

    /**
     * Inicjalizuje przykładowego użytkownika z zahashowanym hasłem
     * {@code "tajneHaslo123"} przed każdym przypadkiem testowym.
     */
    @BeforeEach
    void setUp() {
        mockUser = new User();
        mockUser.setId(1);
        mockUser.setEmail("jan.kowalski@example.com");
        mockUser.setFirstName("Jan");
        mockUser.setLastName("Kowalski");
        mockUser.setRoleId(1);
        mockUser.setPassword(encoder.encode("tajneHaslo123"));
    }

    // -----------------------------------------------------------------------
    // authenticate() – przypadki sukcesu
    // -----------------------------------------------------------------------

    /**
     * Weryfikuje, że metoda {@code authenticate} zwraca użytkownika
     * opakowanego w {@link Optional} gdy podane e-mail i hasło są poprawne.
     */
    @Test
    void authenticate_poprawneCredentials_zwracaUsera() {
        when(userRepository.findByEmail("jan.kowalski@example.com"))
                .thenReturn(Optional.of(mockUser));

        Optional<User> result = authService.authenticate(
                "jan.kowalski@example.com", "tajneHaslo123");

        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo("jan.kowalski@example.com");
    }

    // -----------------------------------------------------------------------
    // authenticate() – przypadki błędów
    // -----------------------------------------------------------------------

    /**
     * Weryfikuje, że metoda {@code authenticate} zwraca pusty {@link Optional}
     * gdy podany e-mail nie istnieje w bazie danych.
     */
    @Test
    void authenticate_zlyEmail_zwracaPuste() {
        when(userRepository.findByEmail("nieistnieje@example.com"))
                .thenReturn(Optional.empty());

        Optional<User> result = authService.authenticate(
                "nieistnieje@example.com", "tajneHaslo123");

        assertThat(result).isEmpty();
    }

    /**
     * Weryfikuje, że metoda {@code authenticate} zwraca pusty {@link Optional}
     * gdy e-mail istnieje, ale podane hasło jest niepoprawne.
     */
    @Test
    void authenticate_zleHaslo_zwracaPuste() {
        when(userRepository.findByEmail("jan.kowalski@example.com"))
                .thenReturn(Optional.of(mockUser));

        Optional<User> result = authService.authenticate(
                "jan.kowalski@example.com", "zleHaslo");

        assertThat(result).isEmpty();
    }

    /**
     * Weryfikuje, że metoda {@code authenticate} zwraca pusty {@link Optional}
     * gdy przekazany e-mail jest pustym ciągiem znaków.
     */
    @Test
    void authenticate_pustyEmail_zwracaPuste() {
        when(userRepository.findByEmail(""))
                .thenReturn(Optional.empty());

        Optional<User> result = authService.authenticate("", "tajneHaslo123");

        assertThat(result).isEmpty();
    }

    /**
     * Weryfikuje, że metoda {@code authenticate} zwraca pusty {@link Optional}
     * gdy przekazane hasło jest pustym ciągiem znaków.
     */
    @Test
    void authenticate_pusteHaslo_zwracaPuste() {
        when(userRepository.findByEmail("jan.kowalski@example.com"))
                .thenReturn(Optional.of(mockUser));

        Optional<User> result = authService.authenticate(
                "jan.kowalski@example.com", "");

        assertThat(result).isEmpty();
    }

    // -----------------------------------------------------------------------
    // Weryfikacja interakcji z repozytorium
    // -----------------------------------------------------------------------

    /**
     * Weryfikuje, że metoda {@code authenticate} wywołuje
     * {@link UserRepository#findByEmail(String)} dokładnie raz
     * z przekazanym adresem e-mail i nie wykonuje żadnych innych
     * operacji na repozytorium.
     */
    @Test
    void authenticate_zawszeSzukaPoEmailu() {
        when(userRepository.findByEmail(any())).thenReturn(Optional.empty());

        authService.authenticate("ktos@example.com", "haslo");

        verify(userRepository, times(1)).findByEmail("ktos@example.com");
        verifyNoMoreInteractions(userRepository);
    }
}

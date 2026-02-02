package com.internship.contact_management_backend.service;

import com.internship.contact_management_backend.entity.User;
import com.internship.contact_management_backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserDetailsServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserDetailsServiceImpl userDetailsService;

    private User testUser;
    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_PASSWORD = "encodedPassword123";

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail(TEST_EMAIL);
        testUser.setPassword(TEST_PASSWORD);
    }

    // ==================== LoadUserByUsername Tests ====================

    @Test
    void loadUserByUsername_WithExistingEmail_ShouldReturnUserDetails() {
        // Arrange
        when(userRepository.findByEmail(TEST_EMAIL))
                .thenReturn(Optional.of(testUser));

        // Act
        UserDetails result = userDetailsService.loadUserByUsername(TEST_EMAIL);

        // Assert
        assertNotNull(result);
        assertEquals(TEST_EMAIL, result.getUsername());
        assertEquals(TEST_PASSWORD, result.getPassword());
        assertNotNull(result.getAuthorities());
        assertTrue(result.isEnabled());
        assertTrue(result.isAccountNonExpired());
        assertTrue(result.isAccountNonLocked());
        assertTrue(result.isCredentialsNonExpired());
        verify(userRepository).findByEmail(TEST_EMAIL);
    }

    @Test
    void loadUserByUsername_WithNonExistingEmail_ShouldThrowUsernameNotFoundException() {
        // Arrange
        String nonExistingEmail = "nonexisting@example.com";
        when(userRepository.findByEmail(nonExistingEmail))
                .thenReturn(Optional.empty());

        // Act & Assert
        UsernameNotFoundException exception = assertThrows(
                UsernameNotFoundException.class,
                () -> userDetailsService.loadUserByUsername(nonExistingEmail)
        );

        assertEquals("Bad Credentials", exception.getMessage());
        verify(userRepository).findByEmail(nonExistingEmail);
    }

    @Test
    void loadUserByUsername_WithNullEmail_ShouldThrowUsernameNotFoundException() {
        // Arrange
        when(userRepository.findByEmail(null))
                .thenReturn(Optional.empty());

        // Act & Assert
        UsernameNotFoundException exception = assertThrows(
                UsernameNotFoundException.class,
                () -> userDetailsService.loadUserByUsername(null)
        );

        assertEquals("Bad Credentials", exception.getMessage());
        verify(userRepository).findByEmail(null);
    }

    @Test
    void loadUserByUsername_WithEmptyEmail_ShouldThrowUsernameNotFoundException() {
        // Arrange
        String emptyEmail = "";
        when(userRepository.findByEmail(emptyEmail))
                .thenReturn(Optional.empty());

        // Act & Assert
        UsernameNotFoundException exception = assertThrows(
                UsernameNotFoundException.class,
                () -> userDetailsService.loadUserByUsername(emptyEmail)
        );

        assertEquals("Bad Credentials", exception.getMessage());
        verify(userRepository).findByEmail(emptyEmail);
    }

    @Test
    void loadUserByUsername_WithWhitespaceEmail_ShouldThrowUsernameNotFoundException() {
        // Arrange
        String whitespaceEmail = "   ";
        when(userRepository.findByEmail(whitespaceEmail))
                .thenReturn(Optional.empty());

        // Act & Assert
        UsernameNotFoundException exception = assertThrows(
                UsernameNotFoundException.class,
                () -> userDetailsService.loadUserByUsername(whitespaceEmail)
        );

        assertEquals("Bad Credentials", exception.getMessage());
        verify(userRepository).findByEmail(whitespaceEmail);
    }

    @Test
    void loadUserByUsername_ShouldCallRepositoryOnce() {
        // Arrange
        when(userRepository.findByEmail(TEST_EMAIL))
                .thenReturn(Optional.of(testUser));

        // Act
        userDetailsService.loadUserByUsername(TEST_EMAIL);

        // Assert
        verify(userRepository, times(1)).findByEmail(TEST_EMAIL);
    }

    @Test
    void loadUserByUsername_WithDifferentEmailCasing_ShouldThrowException() {
        // Arrange
        String upperCaseEmail = "TEST@EXAMPLE.COM";

        // Simulate repository returning empty for different casing
        when(userRepository.findByEmail(upperCaseEmail))
                .thenReturn(Optional.empty());

        // Act & Assert
        UsernameNotFoundException exception = assertThrows(
                UsernameNotFoundException.class,
                () -> userDetailsService.loadUserByUsername(upperCaseEmail)
        );

        assertEquals("Bad Credentials", exception.getMessage());

        verify(userRepository).findByEmail(upperCaseEmail);
    }


    @Test
    void loadUserByUsername_WithSpecialCharactersInEmail_ShouldWork() {
        // Arrange
        String specialEmail = "test+special@example.com";
        User specialUser = new User();
        specialUser.setEmail(specialEmail);
        specialUser.setPassword(TEST_PASSWORD);

        when(userRepository.findByEmail(specialEmail))
                .thenReturn(Optional.of(specialUser));

        // Act
        UserDetails result = userDetailsService.loadUserByUsername(specialEmail);

        // Assert
        assertNotNull(result);
        assertEquals(specialEmail, result.getUsername());
        verify(userRepository).findByEmail(specialEmail);
    }
}
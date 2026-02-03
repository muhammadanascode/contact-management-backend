package com.internship.contact_management_backend.service;

import com.internship.contact_management_backend.dto.UpdatePasswordDto;
import com.internship.contact_management_backend.entity.User;
import com.internship.contact_management_backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setPassword("rawPassword123");
    }

    // ==================== Register Tests ====================

    @Test
    void register_WithValidUser_ShouldRegisterSuccessfully() {
        // Arrange
        String encodedPassword = "encodedPassword123";
        when(userRepository.existsByEmail(testUser.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(testUser.getPassword())).thenReturn(encodedPassword);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        User result = userService.register(testUser);

        // Assert
        assertNotNull(result);
        assertEquals(testUser.getId(), result.getId());
        assertEquals(testUser.getEmail(), result.getEmail());
        verify(userRepository).existsByEmail(testUser.getEmail());
        verify(passwordEncoder).encode("rawPassword123");
        verify(userRepository).save(testUser);
    }

    @Test
    void register_WithExistingEmail_ShouldThrowException() {
        // Arrange
        when(userRepository.existsByEmail(testUser.getEmail())).thenReturn(true);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.register(testUser)
        );

        assertEquals("Email already exists", exception.getMessage());
        verify(userRepository).existsByEmail(testUser.getEmail());
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void register_ShouldEncodePasswordBeforeSaving() {
        // Arrange
        String rawPassword = "rawPassword123";
        String encodedPassword = "encodedPassword123";
        testUser.setPassword(rawPassword);

        when(userRepository.existsByEmail(testUser.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(rawPassword)).thenReturn(encodedPassword);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        userService.register(testUser);

        // Assert
        verify(passwordEncoder).encode(rawPassword);
        assertEquals(encodedPassword, testUser.getPassword());
        verify(userRepository).save(testUser);
    }

    @Test
    void register_WithNullEmail_ShouldThrowException() {
        // Arrange
        testUser.setEmail(null);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.register(testUser)
        );

        // Verify the exception message
        assertEquals("Email cannot be null or empty", exception.getMessage());

        // Verify that repository methods are NOT called
        verify(userRepository, never()).existsByEmail(any());
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
    }


    // ==================== FindByEmail Tests ====================

    @Test
    void findByEmail_WithExistingEmail_ShouldReturnUser() {
        // Arrange
        when(userRepository.findByEmail(testUser.getEmail()))
                .thenReturn(Optional.of(testUser));

        // Act
        User result = userService.findByEmail(testUser.getEmail());

        // Assert
        assertNotNull(result);
        assertEquals(testUser.getId(), result.getId());
        assertEquals(testUser.getEmail(), result.getEmail());
        verify(userRepository).findByEmail(testUser.getEmail());
    }

    @Test
    void findByEmail_WithNonExistingEmail_ShouldReturnNull() {
        // Arrange
        String nonExistingEmail = "nonexisting@example.com";
        when(userRepository.findByEmail(nonExistingEmail))
                .thenReturn(Optional.empty());

        // Act
        User result = userService.findByEmail(nonExistingEmail);

        // Assert
        assertNull(result);
        verify(userRepository).findByEmail(nonExistingEmail);
    }

    @Test
    void findByEmail_WithNullEmail_ShouldReturnNull() {
        // Arrange
        when(userRepository.findByEmail(null))
                .thenReturn(Optional.empty());

        // Act
        User result = userService.findByEmail(null);

        // Assert
        assertNull(result);
        verify(userRepository).findByEmail(null);
    }

    // ==================== UpdatePassword Tests ====================

    @Test
    void updatePassword_WithValidData_ShouldUpdateSuccessfully() {
        // Arrange
        String oldPassword = "oldPassword123";
        String newPassword = "newPassword123";
        String encodedOldPassword = "encodedOldPassword";
        String encodedNewPassword = "encodedNewPassword";

        testUser.setPassword(encodedOldPassword);

        UpdatePasswordDto dto = new UpdatePasswordDto();
        dto.setOldPassword(oldPassword);
        dto.setNewPassword(newPassword);
        dto.setConfirmPassword(newPassword);

        when(userRepository.findByEmail(testUser.getEmail()))
                .thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(oldPassword, encodedOldPassword))
                .thenReturn(true);
        when(passwordEncoder.encode(newPassword))
                .thenReturn(encodedNewPassword);
        when(userRepository.save(any(User.class)))
                .thenReturn(testUser);

        // Act
        userService.updatePassword(testUser.getEmail(), dto);

        // Assert
        verify(passwordEncoder).matches(oldPassword, encodedOldPassword);
        verify(passwordEncoder).encode(newPassword);
        verify(userRepository).save(testUser);
        assertEquals(encodedNewPassword, testUser.getPassword());
    }

    @Test
    void updatePassword_WithMismatchedPasswords_ShouldThrowException() {
        // Arrange
        UpdatePasswordDto dto = new UpdatePasswordDto();
        dto.setOldPassword("oldPassword123");
        dto.setNewPassword("newPassword123");
        dto.setConfirmPassword("differentPassword123");

        String email = testUser.getEmail();

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.updatePassword(email, dto)
        );

        assertEquals("Passwords do not match", exception.getMessage());
        verify(userRepository, never()).findByEmail(anyString());
        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updatePassword_WithIncorrectOldPassword_ShouldThrowException() {
        // Arrange
        String oldPassword = "wrongOldPassword";
        String newPassword = "newPassword123";
        String encodedPassword = "encodedPassword";

        testUser.setPassword(encodedPassword);

        UpdatePasswordDto dto = new UpdatePasswordDto();
        dto.setOldPassword(oldPassword);
        dto.setNewPassword(newPassword);
        dto.setConfirmPassword(newPassword);

        when(userRepository.findByEmail(testUser.getEmail()))
                .thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(oldPassword, encodedPassword))
                .thenReturn(false);

        String email = testUser.getEmail();

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.updatePassword(email, dto)
        );

        assertEquals("Old password is incorrect", exception.getMessage());
        verify(userRepository).findByEmail(testUser.getEmail());
        verify(passwordEncoder).matches(oldPassword, encodedPassword);
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updatePassword_WithNullUser_ShouldThrowNullPointerException() {
        // Arrange
        UpdatePasswordDto dto = new UpdatePasswordDto();
        dto.setOldPassword("oldPassword123");
        dto.setNewPassword("newPassword123");
        dto.setConfirmPassword("newPassword123");

        when(userRepository.findByEmail(testUser.getEmail()))
                .thenReturn(Optional.empty());

        String email = testUser.getEmail();

        // Act & Assert
        assertThrows(
                NullPointerException.class,
                () -> userService.updatePassword(email, dto)
        );

        verify(userRepository).findByEmail(testUser.getEmail());
        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    // java
    @Test
    void updatePassword_WithEmptyNewPassword_ShouldThrowException() {
        // Arrange
        UpdatePasswordDto dto = new UpdatePasswordDto();
        dto.setOldPassword("oldPassword123");
        dto.setNewPassword("");
        dto.setConfirmPassword("");

        testUser.setPassword("encodedOldPassword");

        String email = testUser.getEmail();

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.updatePassword(email, dto)
        );

        assertEquals("New password cannot be empty", exception.getMessage());

        // Verify that encode and save were never called
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
    }


    @Test
    void updatePassword_ShouldEncodeNewPasswordBeforeSaving() {
        // Arrange
        String oldPassword = "oldPassword123";
        String newPassword = "newPassword123";
        String encodedOldPassword = "encodedOldPassword";
        String encodedNewPassword = "encodedNewPassword";

        testUser.setPassword(encodedOldPassword);

        UpdatePasswordDto dto = new UpdatePasswordDto();
        dto.setOldPassword(oldPassword);
        dto.setNewPassword(newPassword);
        dto.setConfirmPassword(newPassword);

        when(userRepository.findByEmail(testUser.getEmail()))
                .thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(oldPassword, encodedOldPassword))
                .thenReturn(true);
        when(passwordEncoder.encode(newPassword))
                .thenReturn(encodedNewPassword);
        when(userRepository.save(any(User.class)))
                .thenReturn(testUser);

        // Act
        userService.updatePassword(testUser.getEmail(), dto);

        // Assert
        verify(passwordEncoder).encode(newPassword);
        assertEquals(encodedNewPassword, testUser.getPassword());
    }
}
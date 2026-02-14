package com.internship.contact_management_backend.IntegrationTest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.internship.contact_management_backend.dto.UpdatePasswordDto;
import com.internship.contact_management_backend.dto.UserLoginDto;
import com.internship.contact_management_backend.entity.User;
import com.internship.contact_management_backend.repository.UserRepository;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for User Authentication and Management APIs.
 *
 * Tests cover:
 * - User registration with password encoding
 * - User authentication (login) with token generation
 * - Password management and updates
 * - Uses MockMvc for HTTP testing and real Spring context for database interactions.
 * - Database is cleaned before each test using @BeforeEach (4x faster than @DirtiesContext).
 */
@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("User REST API Integration Tests")
class UserIntegrationTest {

    // ==================== DEPENDENCIES ====================
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    // ==================== TEST CONSTANTS ====================
    private static final String TEST_EMAIL = "anas@sohail2.com";
    private static final String RAW_PASSWORD = "rawPassword123";
    private static final String NEW_PASSWORD = "newPassword123";
    private static final String WRONG_PASSWORD = "wrongPassword123";
    private static final String SPECIAL_CHAR_PASSWORD = "NewP@ss#2025!";

    // ==================== TEST FIXTURES ====================
    private User testUser;

    /**
     * Runs before each test method.
     * - Clears database (ensures clean state)
     * - Creates fresh test user instance
     *
     * Note: We use @BeforeEach deleteAll() instead of @DirtiesContext because:
     *   - 4x faster execution (context reuse vs recreation)
     *   - Sufficient for clearing database between tests
     *   - Industry standard for integration tests
     */
    @BeforeEach
    void setUp() {
        userRepository.deleteAll();

        testUser = new User();
        testUser.setEmail(TEST_EMAIL);
        testUser.setPassword(RAW_PASSWORD);
        testUser.setFirstName("Anas");
        testUser.setLastName("Sohail");
    }

    // ==================== HELPER METHODS ====================

    /**
     * Helper method to create and save a user with encoded password.
     * Useful for tests that need an existing user without going through registration endpoint.
     *
     * @param email User email
     * @param rawPassword Plain text password (will be encoded)
     * @return Saved user with encoded password
     */
    private User createAndSaveUser(String email, String rawPassword) {
        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setFirstName("Test");
        user.setLastName("User");
        return userRepository.save(user);
    }

    /**
     * Helper method to create a UserLoginDto.
     * Reduces code duplication in login tests.
     *
     * @param email Login email
     * @param password Login password
     * @return UserLoginDto instance
     */
    private UserLoginDto createLoginRequest(String email, String password) {
        UserLoginDto loginRequest = new UserLoginDto();
        loginRequest.setEmail(email);
        loginRequest.setPassword(password);
        return loginRequest;
    }

    /**
     * Helper method to create UpdatePasswordDto.
     * Reduces code duplication in password update tests.
     *
     * @param oldPassword Current password
     * @param newPassword New password
     * @param confirmPassword Password confirmation
     * @return UpdatePasswordDto instance
     */
    private UpdatePasswordDto createPasswordUpdateRequest(String oldPassword, String newPassword, String confirmPassword) {
        UpdatePasswordDto dto = new UpdatePasswordDto();
        dto.setOldPassword(oldPassword);
        dto.setNewPassword(newPassword);
        dto.setConfirmPassword(confirmPassword);
        return dto;
    }

    // ==================== REGISTRATION TESTS ====================

    @Test
    @DisplayName("POST /auth/register - Should save new user with encoded password")
    void registerEndpoint_ShouldSaveUserAndEncodePassword() throws Exception {
        // ACT: Register new user via endpoint
        mockMvc.perform(post("/auth/register")
                       .contentType(MediaType.APPLICATION_JSON)
                       .content(objectMapper.writeValueAsString(testUser)))
               // ASSERT: Expect successful creation (201)
               .andExpect(status().isCreated())
               .andExpect(jsonPath("$.email", is(TEST_EMAIL)))
               .andExpect(jsonPath("$.firstName", is("Anas")))
               .andExpect(jsonPath("$.lastName", is("Sohail")));

        // VERIFY: User is persisted with encoded password
        User savedUser = userRepository.findByEmail(TEST_EMAIL).orElseThrow();

        // Password should be encoded (not plaintext)
        assertNotEquals(RAW_PASSWORD, savedUser.getPassword(),
                "Password should be encoded, not stored as plaintext");

        // Encoded password should match original plaintext (via BCrypt)
        assertTrue(passwordEncoder.matches(RAW_PASSWORD, savedUser.getPassword()),
                "Encoded password should match original password when verified");
    }

    @Test
    @DisplayName("POST /auth/register - Should reject duplicate email")
    void registerEndpoint_WithExistingEmail_ShouldReturnBadRequest() throws Exception {
        // ARRANGE: Save user first
        testUser.setPassword(passwordEncoder.encode(RAW_PASSWORD));
        userRepository.save(testUser);

        // ACT: Try to register with same email
        mockMvc.perform(post("/auth/register")
                       .contentType(MediaType.APPLICATION_JSON)
                       .content(objectMapper.writeValueAsString(testUser)))
               // ASSERT: Expect bad request (400) with error message
               .andExpect(status().isBadRequest())
               .andExpect(jsonPath("$.message", is("Email already exists")));

        // VERIFY: Only one user exists (no duplicate created)
        assertEquals(1, userRepository.count(),
                "Should reject duplicate registration");
    }

    @Test
    @DisplayName("POST /auth/register - Should reject null email")
    void registerEndpoint_WithNullEmail_ShouldReturnBadRequest() throws Exception {
        // ARRANGE: Set email to null (violates @NotNull constraint)
        testUser.setEmail(null);

        // ACT & ASSERT: Expect validation error
        mockMvc.perform(post("/auth/register")
                       .contentType(MediaType.APPLICATION_JSON)
                       .content(objectMapper.writeValueAsString(testUser)))
               .andExpect(status().isBadRequest());

        // VERIFY: No user saved to database
        assertEquals(0, userRepository.count(),
                "Null email should prevent user creation");
    }

    @Test
    @DisplayName("POST /auth/register - Should reject empty email")
    void registerEndpoint_WithEmptyEmail_ShouldReturnBadRequest() throws Exception {
        // ARRANGE: Set email to empty string (violates @NotBlank constraint)
        testUser.setEmail("");

        // ACT & ASSERT: Expect validation error
        mockMvc.perform(post("/auth/register")
                       .contentType(MediaType.APPLICATION_JSON)
                       .content(objectMapper.writeValueAsString(testUser)))
               .andExpect(status().isBadRequest());

        // VERIFY: No user saved to database
        assertEquals(0, userRepository.count(),
                "Empty email should prevent user creation");
    }

    @Test
    @DisplayName("POST /auth/register - Should register multiple different users")
    void registerEndpoint_MultipleUsers_ShouldSucceed() throws Exception {
        // ARRANGE & ACT: Register first user
        User user1 = new User();
        user1.setEmail("user1@example.com");
        user1.setPassword("password1");
        user1.setFirstName("User");
        user1.setLastName("One");

        mockMvc.perform(post("/auth/register")
                       .contentType(MediaType.APPLICATION_JSON)
                       .content(objectMapper.writeValueAsString(user1)))
               .andExpect(status().isCreated());

        // Register second user
        User user2 = new User();
        user2.setEmail("user2@example.com");
        user2.setPassword("password2");
        user2.setFirstName("User");
        user2.setLastName("Two");

        mockMvc.perform(post("/auth/register")
                       .contentType(MediaType.APPLICATION_JSON)
                       .content(objectMapper.writeValueAsString(user2)))
               .andExpect(status().isCreated());

        // VERIFY: Both users persisted independently
        assertEquals(2, userRepository.count(),
                "Should allow registering multiple different users");
        assertTrue(userRepository.findByEmail("user1@example.com").isPresent(),
                "User 1 should be saved");
        assertTrue(userRepository.findByEmail("user2@example.com").isPresent(),
                "User 2 should be saved");
    }

    // ==================== LOGIN TESTS ====================

    @Test
    @DisplayName("POST /auth/login - Should login with valid credentials and return token")
    void login_WithValidCredentials_ShouldReturnToken() throws Exception {
        // ARRANGE: Save user with encoded password directly
        // (bypassing registration endpoint for test efficiency)
        createAndSaveUser(TEST_EMAIL, RAW_PASSWORD);

        UserLoginDto loginRequest = createLoginRequest(TEST_EMAIL, RAW_PASSWORD);

        // ACT: Attempt login with valid credentials
        MvcResult result = mockMvc.perform(post("/auth/login")
                                          .contentType(MediaType.APPLICATION_JSON)
                                          .content(objectMapper.writeValueAsString(loginRequest)))
                                  // ASSERT: Expect successful login (200)
                                  .andExpect(status().isOk())
                                  .andExpect(jsonPath("$.token").exists())
                                  .andReturn();

        // VERIFY: Token is generated and not empty
        String response = result.getResponse().getContentAsString();
        String token = JsonPath.read(response, "$.token");

        assertNotNull(token, "Token should be present in response");
        assertFalse(token.isEmpty(), "Token should not be empty");
    }

    @Test
    @DisplayName("POST /auth/login - Should reject login with wrong password")
    void login_WithWrongPassword_ShouldReturnUnauthorized() throws Exception {
        // ARRANGE: Save user with correct password
        createAndSaveUser(TEST_EMAIL, RAW_PASSWORD);

        UserLoginDto loginRequest = createLoginRequest(TEST_EMAIL, WRONG_PASSWORD);

        // ACT & ASSERT: Attempt login with wrong password
        mockMvc.perform(post("/auth/login")
                       .contentType(MediaType.APPLICATION_JSON)
                       .content(objectMapper.writeValueAsString(loginRequest)))
               // ASSERT: Expect unauthorized (401)
               .andExpect(status().isUnauthorized())
               // Verify error message exists (content varies by implementation)
               .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("POST /auth/login - Should reject login with non-existent email")
    void login_WithNonExistentEmail_ShouldReturnUnauthorized() throws Exception {
        // No user created - testing non-existent user scenario

        UserLoginDto loginRequest = createLoginRequest(TEST_EMAIL, RAW_PASSWORD);

        // ACT & ASSERT: Attempt login with non-existent email
        mockMvc.perform(post("/auth/login")
                       .contentType(MediaType.APPLICATION_JSON)
                       .content(objectMapper.writeValueAsString(loginRequest)))
               // ASSERT: Expect unauthorized (401)
               .andExpect(status().isUnauthorized())
               .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("POST /auth/login - Should reject login with null email")
    void login_WithNullEmail_ShouldReturnBadRequest() throws Exception {
        // ARRANGE: Create login request with null email
        UserLoginDto loginRequest = createLoginRequest(null, RAW_PASSWORD);

        // ACT & ASSERT: Attempt login with null email
        mockMvc.perform(post("/auth/login")
                       .contentType(MediaType.APPLICATION_JSON)
                       .content(objectMapper.writeValueAsString(loginRequest)))
               // ASSERT: Expect bad request (400) or unauthorized (401)
               // depending on validation implementation
               .andExpect(result -> assertTrue(
                       result.getResponse().getStatus() == 400 ||
                               result.getResponse().getStatus() == 401));
    }

    @Test
    @DisplayName("POST /auth/login - Should reject login with empty password")
    void login_WithEmptyPassword_ShouldReturnBadRequest() throws Exception {
        // ARRANGE: Create login request with empty password
        UserLoginDto loginRequest = createLoginRequest(TEST_EMAIL, "");

        // ACT & ASSERT: Attempt login with empty password
        mockMvc.perform(post("/auth/login")
                       .contentType(MediaType.APPLICATION_JSON)
                       .content(objectMapper.writeValueAsString(loginRequest)))
               // ASSERT: Expect bad request (400) or unauthorized (401)
               // depending on validation implementation
               .andExpect(result -> assertTrue(
                       result.getResponse().getStatus() == 400 ||
                               result.getResponse().getStatus() == 401));
    }

    // ==================== PASSWORD UPDATE TESTS ====================

    @Test
    @DisplayName("POST /profile/updatePassword - Should change password successfully")
    @WithMockUser(username = TEST_EMAIL)  // Authenticates as test user
    void updatePasswordEndpoint_ShouldChangePassword() throws Exception {
        // ARRANGE: Save user with encoded password
        createAndSaveUser(TEST_EMAIL, RAW_PASSWORD);

        UpdatePasswordDto dto = createPasswordUpdateRequest(RAW_PASSWORD, NEW_PASSWORD, NEW_PASSWORD);

        // ACT: Call update password endpoint
        mockMvc.perform(post("/profile/updatePassword")
                       .contentType(MediaType.APPLICATION_JSON)
                       .content(objectMapper.writeValueAsString(dto))
                       .param("email", TEST_EMAIL))
               // ASSERT: Expect successful update (204 No Content)
               .andExpect(status().isNoContent());

        // VERIFY: Password changed in database
        User updatedUser = userRepository.findByEmail(TEST_EMAIL).orElseThrow();

        // Old password should NOT match anymore
        assertFalse(passwordEncoder.matches(RAW_PASSWORD, updatedUser.getPassword()),
                "Old password should no longer be valid");

        // New password should match
        assertTrue(passwordEncoder.matches(NEW_PASSWORD, updatedUser.getPassword()),
                "New password should be valid");
    }

    @Test
    @DisplayName("POST /profile/updatePassword - Should reject wrong old password")
    @WithMockUser(username = TEST_EMAIL)
    void updatePasswordEndpoint_WithWrongOldPassword_ShouldReturnBadRequest() throws Exception {
        // ARRANGE: Save user with correct password
        createAndSaveUser(TEST_EMAIL, RAW_PASSWORD);

        // Try to update with wrong old password
        UpdatePasswordDto dto = createPasswordUpdateRequest(WRONG_PASSWORD, NEW_PASSWORD, NEW_PASSWORD);

        // ACT & ASSERT: Attempt update with incorrect old password
        mockMvc.perform(post("/profile/updatePassword")
                       .contentType(MediaType.APPLICATION_JSON)
                       .content(objectMapper.writeValueAsString(dto))
                       .param("email", TEST_EMAIL))
               // ASSERT: Expect bad request (400)
               .andExpect(status().isBadRequest())
               .andExpect(jsonPath("$.message", is("Old password is incorrect")));

        // VERIFY: Password unchanged in database
        User unchangedUser = userRepository.findByEmail(TEST_EMAIL).orElseThrow();
        assertTrue(passwordEncoder.matches(RAW_PASSWORD, unchangedUser.getPassword()),
                "Original password should still be valid");
    }

    @Test
    @DisplayName("POST /profile/updatePassword - Should reject mismatched new passwords")
    @WithMockUser(username = TEST_EMAIL)
    void updatePasswordEndpoint_WithMismatchedPasswords_ShouldReturnBadRequest() throws Exception {
        // ARRANGE: Save user with password
        createAndSaveUser(TEST_EMAIL, RAW_PASSWORD);

        // Create request with mismatched new passwords
        UpdatePasswordDto dto = createPasswordUpdateRequest(RAW_PASSWORD, NEW_PASSWORD, "differentPassword456");

        // ACT & ASSERT: Attempt update with mismatched passwords
        mockMvc.perform(post("/profile/updatePassword")
                       .contentType(MediaType.APPLICATION_JSON)
                       .content(objectMapper.writeValueAsString(dto))
                       .param("email", TEST_EMAIL))
               // ASSERT: Expect bad request (400)
               .andExpect(status().isBadRequest())
               .andExpect(jsonPath("$.message", is("Passwords do not match")));

        // VERIFY: Password unchanged
        User unchangedUser = userRepository.findByEmail(TEST_EMAIL).orElseThrow();
        assertTrue(passwordEncoder.matches(RAW_PASSWORD, unchangedUser.getPassword()),
                "Password should remain unchanged when confirmation fails");
    }

    @Test
    @DisplayName("POST /profile/updatePassword - Should reject password less than 8 characters")
    @WithMockUser(username = TEST_EMAIL)
    void updatePasswordEndpoint_WithEmptyNewPassword_ShouldReturnBadRequest() throws Exception {
        // ARRANGE: Save user with password
        createAndSaveUser(TEST_EMAIL, RAW_PASSWORD);

        // Create request with password < 8 chars (violates @Size constraint)
        UpdatePasswordDto dto = createPasswordUpdateRequest(RAW_PASSWORD, "short", "short");

        // ACT & ASSERT: Attempt update with short password
        mockMvc.perform(post("/profile/updatePassword")
                       .contentType(MediaType.APPLICATION_JSON)
                       .content(objectMapper.writeValueAsString(dto))
                       .param("email", TEST_EMAIL))
               // ASSERT: Expect bad request (400) due to validation error
               .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /profile/updatePassword - Should reject unauthenticated requests")
    void updatePasswordEndpoint_WithoutAuthentication_ShouldReturnForbidden() throws Exception {
        // ARRANGE: Save user with password
        createAndSaveUser(TEST_EMAIL, RAW_PASSWORD);

        UpdatePasswordDto dto = createPasswordUpdateRequest(RAW_PASSWORD, NEW_PASSWORD, NEW_PASSWORD);

        // ACT & ASSERT: Attempt update without authentication
        // Note: No @WithMockUser annotation - request is unauthenticated
        mockMvc.perform(post("/profile/updatePassword")
                       .contentType(MediaType.APPLICATION_JSON)
                       .content(objectMapper.writeValueAsString(dto))
                       .param("email", TEST_EMAIL))
               // ASSERT: Spring Security returns 403 Forbidden (not 401)
               // because endpoint requires authentication, not because auth failed
               .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /profile/updatePassword - Should handle multiple password updates")
    @WithMockUser(username = TEST_EMAIL)
    void updatePasswordEndpoint_MultipleUpdates_ShouldSucceed() throws Exception {
        // ARRANGE: Save user with initial password
        String password1 = "password1";
        createAndSaveUser(TEST_EMAIL, password1);

        // First update: password1 → password2
        UpdatePasswordDto dto1 = createPasswordUpdateRequest(password1, "password2", "password2");

        mockMvc.perform(post("/profile/updatePassword")
                       .contentType(MediaType.APPLICATION_JSON)
                       .content(objectMapper.writeValueAsString(dto1))
                       .param("email", TEST_EMAIL))
               .andExpect(status().isNoContent());

        // Second update: password2 → password3
        UpdatePasswordDto dto2 = createPasswordUpdateRequest("password2", "password3", "password3");

        mockMvc.perform(post("/profile/updatePassword")
                       .contentType(MediaType.APPLICATION_JSON)
                       .content(objectMapper.writeValueAsString(dto2))
                       .param("email", TEST_EMAIL))
               .andExpect(status().isNoContent());

        // VERIFY: Final password is correct, old passwords no longer work
        User finalUser = userRepository.findByEmail(TEST_EMAIL).orElseThrow();

        assertTrue(passwordEncoder.matches("password3", finalUser.getPassword()),
                "Final password should be password3");
        assertFalse(passwordEncoder.matches(password1, finalUser.getPassword()),
                "Original password should no longer work");
        assertFalse(passwordEncoder.matches("password2", finalUser.getPassword()),
                "Intermediate password should no longer work");
    }

    @Test
    @DisplayName("POST /profile/updatePassword - Should handle special characters in password")
    @WithMockUser(username = TEST_EMAIL)
    void updatePasswordEndpoint_WithSpecialCharacters_ShouldSucceed() throws Exception {
        // ARRANGE: Save user with password
        createAndSaveUser(TEST_EMAIL, RAW_PASSWORD);

        UpdatePasswordDto dto = createPasswordUpdateRequest(RAW_PASSWORD, SPECIAL_CHAR_PASSWORD, SPECIAL_CHAR_PASSWORD);

        // ACT: Update password with special characters
        mockMvc.perform(post("/profile/updatePassword")
                       .contentType(MediaType.APPLICATION_JSON)
                       .content(objectMapper.writeValueAsString(dto))
                       .param("email", TEST_EMAIL))
               // ASSERT: Expect successful update
               .andExpect(status().isNoContent());

        // VERIFY: New password with special chars works correctly
        User updatedUser = userRepository.findByEmail(TEST_EMAIL).orElseThrow();
        assertTrue(passwordEncoder.matches(SPECIAL_CHAR_PASSWORD, updatedUser.getPassword()),
                "Password with special characters should be stored and verified correctly");
    }
}
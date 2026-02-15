package com.internship.contact_management_backend.IntegrationTest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.internship.contact_management_backend.dto.ContactDto;
import com.internship.contact_management_backend.entity.Contact;
import com.internship.contact_management_backend.entity.User;
import com.internship.contact_management_backend.repository.ContactRepository;
import com.internship.contact_management_backend.repository.UserRepository;
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

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ContactIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ContactRepository contactRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String TEST_EMAIL = "test@example.com";
    private static final String PASSWORD = "password123";

    private User testUser;

    // =====================================================
    // SETUP
    // =====================================================

    @BeforeEach
    void setUp() {

        contactRepository.deleteAll();
        userRepository.deleteAll();

        testUser = new User();
        testUser.setEmail(TEST_EMAIL);
        testUser.setPassword(passwordEncoder.encode(PASSWORD));
        testUser.setFirstName("Test");
        testUser.setLastName("User");

        userRepository.save(testUser);
    }

    // =====================================================
    // HELPER METHODS
    // =====================================================

    private Contact createContactForTest(String firstName) {

        Contact contact = new Contact();
        contact.setFirstName(firstName);
        contact.setLastName("Doe");
        contact.setEmail(firstName.toLowerCase() + "@example.com");
        contact.setEmailLabel("Work");
        contact.setPhoneNumber("+923242650627");
        contact.setPhoneNumberLabel("Mobile");
        contact.setUser(testUser);

        return contactRepository.save(contact);
    }

    private ContactDto buildValidContactDto(String firstName) {

        ContactDto dto = new ContactDto();
        dto.setFirstName(firstName);
        dto.setLastName("Doe");
        dto.setEmail(firstName.toLowerCase() + "@example.com");
        dto.setEmailLabel("Work");
        dto.setPhoneNumber("+923242650627");
        dto.setPhoneNumberLabel("Mobile");

        return dto;
    }

    // =====================================================
    // CREATE CONTACT
    // =====================================================

    @Test
    @WithMockUser(username = TEST_EMAIL)
    @DisplayName("POST /contacts/create - Should create contact")
    void createContact_ShouldCreateSuccessfully() throws Exception {

        ContactDto dto = buildValidContactDto("John");

        mockMvc.perform(post("/contacts/create")
                       .contentType(MediaType.APPLICATION_JSON)
                       .content(objectMapper.writeValueAsString(dto)))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.firstName", is("John")))
               .andExpect(jsonPath("$.email", is("john@example.com")));

        List<Contact> contacts = contactRepository.findByUserId(testUser.getId());
        assertEquals(1, contacts.size());
    }

    // =====================================================
    // GET ALL CONTACTS
    // =====================================================

    @Test
    @WithMockUser(username = TEST_EMAIL)
    @DisplayName("GET /contacts/getAll - Should return user contacts")
    void getAllContacts_ShouldReturnList() throws Exception {

        createContactForTest("John");

        mockMvc.perform(get("/contacts/getAll"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$", hasSize(1)));
    }

    // =====================================================
    // SEARCH CONTACTS
    // =====================================================

    @Test
    @WithMockUser(username = TEST_EMAIL)
    @DisplayName("GET /contacts/getAll?name=John - Should return filtered contacts")
    void searchContacts_ShouldReturnMatchingContacts() throws Exception {

        createContactForTest("John");
        createContactForTest("Alice");

        mockMvc.perform(get("/contacts/getAll")
                       .param("name", "John"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$", hasSize(1)))
               .andExpect(jsonPath("$[0].firstName", is("John")));
    }

    // =====================================================
    // UPDATE CONTACT
    // =====================================================

    @Test
    @WithMockUser(username = TEST_EMAIL)
    @DisplayName("PUT /contacts/update/{id} - Should update contact")
    void updateContact_ShouldUpdateSuccessfully() throws Exception {

        Contact contact = createContactForTest("Old");

        ContactDto updatedDto = new ContactDto();
        updatedDto.setFirstName("Updated");
        updatedDto.setLastName("Name");
        updatedDto.setEmail("updated@example.com");
        updatedDto.setEmailLabel("Work");
        updatedDto.setPhoneNumber("+923242650627");
        updatedDto.setPhoneNumberLabel("Mobile");

        mockMvc.perform(put("/contacts/update/" + contact.getId())
                       .contentType(MediaType.APPLICATION_JSON)
                       .content(objectMapper.writeValueAsString(updatedDto)))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.firstName", is("Updated")));

        Contact updated = contactRepository.findById(contact.getId()).orElseThrow();
        assertEquals("Updated", updated.getFirstName());
    }

    // =====================================================
    // DELETE CONTACT
    // =====================================================

    @Test
    @WithMockUser(username = TEST_EMAIL)
    @DisplayName("DELETE /contacts/delete/{id} - Should delete contact")
    void deleteContact_ShouldDeleteSuccessfully() throws Exception {

        Contact contact = createContactForTest("Delete");

        mockMvc.perform(delete("/contacts/delete/" + contact.getId()))
               .andExpect(status().isNoContent());

        assertFalse(contactRepository.findById(contact.getId()).isPresent());
    }

    // =====================================================
    // UNAUTHORIZED ACCESS
    // =====================================================

    @Test
    @DisplayName("POST /contacts/create - Should return 401 if not authenticated")
    void createContact_WithoutAuthentication_ShouldReturnUnauthorized() throws Exception {

        ContactDto dto = buildValidContactDto("John");

        mockMvc.perform(post("/contacts/create")
                       .contentType(MediaType.APPLICATION_JSON)
                       .content(objectMapper.writeValueAsString(dto)))
               .andExpect(status().isForbidden());
    }
}

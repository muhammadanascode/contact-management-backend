package com.internship.contact_management_backend.service;

import com.internship.contact_management_backend.entity.Contact;
import com.internship.contact_management_backend.entity.User;
import com.internship.contact_management_backend.exception.ResourceNotFoundException;
import com.internship.contact_management_backend.repository.ContactRepository;
import com.internship.contact_management_backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ContactServiceTest {

    @Mock
    private ContactRepository contactRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ContactService contactService;

    private User testUser;
    private Contact testContact;
    private static final String TEST_EMAIL = "test@example.com";
    private static final Long TEST_USER_ID = 1L;
    private static final Long TEST_CONTACT_ID = 100L;
    private static final String BAD_CREDENTIALS = "Bad Credentials";

    @BeforeEach
    void setUp() {
        // Setup test user
        testUser = new User();
        testUser.setId(TEST_USER_ID);
        testUser.setEmail(TEST_EMAIL);
        testUser.setPassword("encodedPassword");

        // Setup test contact
        testContact = new Contact();
        testContact.setId(TEST_CONTACT_ID);
        testContact.setFirstName("John");
        testContact.setLastName("Doe");
        testContact.setEmail("john.doe@example.com");
        testContact.setEmailLabel("Work");
        testContact.setPhoneNumber("1234567890");
        testContact.setPhoneNumberLabel("Mobile");
        testContact.setUser(testUser);
    }

    // ==================== CreateContact Tests ====================

    @Test
    void createContact_WithValidData_ShouldCreateSuccessfully() {
        // Arrange
        Contact newContact = new Contact();
        newContact.setFirstName("Jane");
        newContact.setLastName("Smith");

        when(userRepository.findByEmail(TEST_EMAIL))
                .thenReturn(Optional.of(testUser));
        when(contactRepository.save(any(Contact.class)))
                .thenReturn(testContact);

        // Act
        Contact result = contactService.createContact(newContact, TEST_EMAIL);

        // Assert
        assertNotNull(result);
        assertEquals(testContact.getId(), result.getId());
        assertEquals(testUser, newContact.getUser());
        verify(userRepository).findByEmail(TEST_EMAIL);
        verify(contactRepository).save(newContact);
    }

    @Test
    void createContact_WithNonExistingUser_ShouldThrowUsernameNotFoundException() {
        // Arrange
        Contact newContact = new Contact();
        when(userRepository.findByEmail(TEST_EMAIL))
                .thenReturn(Optional.empty());

        // Act & Assert
        UsernameNotFoundException exception = assertThrows(
                UsernameNotFoundException.class,
                () -> contactService.createContact(newContact, TEST_EMAIL)
        );

        assertEquals(BAD_CREDENTIALS, exception.getMessage());
        verify(userRepository).findByEmail(TEST_EMAIL);
        verify(contactRepository, never()).save(any(Contact.class));
    }

    @Test
    void createContact_WithNullEmail_ShouldThrowUsernameNotFoundException() {
        // Arrange
        Contact newContact = new Contact();
        when(userRepository.findByEmail(null))
                .thenReturn(Optional.empty());

        // Act & Assert
        UsernameNotFoundException exception = assertThrows(
                UsernameNotFoundException.class,
                () -> contactService.createContact(newContact, null)
        );

        assertEquals(BAD_CREDENTIALS, exception.getMessage());
    }

    // ==================== GetContactsByEmail Tests ====================

    @Test
    void getContactsByEmail_WithExistingUser_ShouldReturnContactsList() {
        // Arrange
        List<Contact> contacts = Arrays.asList(testContact, new Contact());
        when(userRepository.findByEmail(TEST_EMAIL))
                .thenReturn(Optional.of(testUser));
        when(contactRepository.findByUserId(TEST_USER_ID))
                .thenReturn(contacts);

        // Act
        List<Contact> result = contactService.getContactsByEmail(TEST_EMAIL);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(userRepository).findByEmail(TEST_EMAIL);
        verify(contactRepository).findByUserId(TEST_USER_ID);
    }

    @Test
    void getContactsByEmail_WithNonExistingUser_ShouldThrowUsernameNotFoundException() {
        // Arrange
        when(userRepository.findByEmail(TEST_EMAIL))
                .thenReturn(Optional.empty());

        // Act & Assert
        UsernameNotFoundException exception = assertThrows(
                UsernameNotFoundException.class,
                () -> contactService.getContactsByEmail(TEST_EMAIL)
        );

        assertEquals(BAD_CREDENTIALS, exception.getMessage());
        verify(userRepository).findByEmail(TEST_EMAIL);
        verify(contactRepository, never()).findByUserId(anyLong());
    }

    @Test
    void getContactsByEmail_WithNoContacts_ShouldReturnEmptyList() {
        // Arrange
        when(userRepository.findByEmail(TEST_EMAIL))
                .thenReturn(Optional.of(testUser));
        when(contactRepository.findByUserId(TEST_USER_ID))
                .thenReturn(new ArrayList<>());

        // Act
        List<Contact> result = contactService.getContactsByEmail(TEST_EMAIL);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(contactRepository).findByUserId(TEST_USER_ID);
    }

    // ==================== DeleteContact Tests ====================

    @Test
    void deleteContact_WithValidData_ShouldDeleteSuccessfully() {
        // Arrange
        when(userRepository.findByEmail(TEST_EMAIL))
                .thenReturn(Optional.of(testUser));
        when(contactRepository.findContactById(TEST_CONTACT_ID))
                .thenReturn(Optional.of(testContact));

        // Act
        contactService.deleteContact(TEST_CONTACT_ID, TEST_EMAIL);

        // Assert
        verify(userRepository).findByEmail(TEST_EMAIL);
        verify(contactRepository).findContactById(TEST_CONTACT_ID);
        verify(contactRepository).delete(testContact);
    }

    @Test
    void deleteContact_WithNonExistingUser_ShouldThrowUsernameNotFoundException() {
        // Arrange
        when(userRepository.findByEmail(TEST_EMAIL))
                .thenReturn(Optional.empty());

        // Act & Assert
        UsernameNotFoundException exception = assertThrows(
                UsernameNotFoundException.class,
                () -> contactService.deleteContact(TEST_CONTACT_ID, TEST_EMAIL)
        );

        assertEquals(BAD_CREDENTIALS, exception.getMessage());
        verify(userRepository).findByEmail(TEST_EMAIL);
        verify(contactRepository, never()).findContactById(anyLong());
        verify(contactRepository, never()).delete(any(Contact.class));
    }

    @Test
    void deleteContact_WithNonExistingContact_ShouldThrowResourceNotFoundException() {
        // Arrange
        when(userRepository.findByEmail(TEST_EMAIL))
                .thenReturn(Optional.of(testUser));
        when(contactRepository.findContactById(TEST_CONTACT_ID))
                .thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> contactService.deleteContact(TEST_CONTACT_ID, TEST_EMAIL)
        );

        assertTrue(exception.getMessage().contains("Contact not found"));
        assertTrue(exception.getMessage().contains(TEST_CONTACT_ID.toString()));
        verify(contactRepository).findContactById(TEST_CONTACT_ID);
        verify(contactRepository, never()).delete(any(Contact.class));
    }

    @Test
    void deleteContact_WithContactNotBelongingToUser_ShouldThrowBadCredentialsException() {
        // Arrange
        User differentUser = new User();
        differentUser.setId(999L);
        differentUser.setEmail("different@example.com");

        Contact otherUsersContact = new Contact();
        otherUsersContact.setId(TEST_CONTACT_ID);
        otherUsersContact.setUser(differentUser);

        when(userRepository.findByEmail(TEST_EMAIL))
                .thenReturn(Optional.of(testUser));
        when(contactRepository.findContactById(TEST_CONTACT_ID))
                .thenReturn(Optional.of(otherUsersContact));

        // Act & Assert
        BadCredentialsException exception = assertThrows(
                BadCredentialsException.class,
                () -> contactService.deleteContact(TEST_CONTACT_ID, TEST_EMAIL)
        );

        assertEquals(BAD_CREDENTIALS, exception.getMessage());
        verify(contactRepository, never()).delete(any(Contact.class));
    }

    // ==================== UpdateContact Tests ====================

    @Test
    void updateContact_WithValidData_ShouldUpdateSuccessfully() {
        // Arrange
        Contact updatedData = new Contact();
        updatedData.setFirstName("Updated");
        updatedData.setLastName("Name");
        updatedData.setEmail("updated@example.com");
        updatedData.setEmailLabel("Personal");
        updatedData.setPhoneNumber("9876543210");
        updatedData.setPhoneNumberLabel("Home");

        when(userRepository.findByEmail(TEST_EMAIL))
                .thenReturn(Optional.of(testUser));
        when(contactRepository.findContactById(TEST_CONTACT_ID))
                .thenReturn(Optional.of(testContact));
        when(contactRepository.save(any(Contact.class)))
                .thenReturn(testContact);

        // Act
        Contact result = contactService.updateContact(TEST_CONTACT_ID, updatedData, TEST_EMAIL);

        // Assert
        assertNotNull(result);
        assertEquals("Updated", testContact.getFirstName());
        assertEquals("Name", testContact.getLastName());
        assertEquals("updated@example.com", testContact.getEmail());
        assertEquals("Personal", testContact.getEmailLabel());
        assertEquals("9876543210", testContact.getPhoneNumber());
        assertEquals("Home", testContact.getPhoneNumberLabel());
        verify(contactRepository).save(testContact);
    }

    @Test
    void updateContact_WithNonExistingUser_ShouldThrowUsernameNotFoundException() {
        // Arrange
        Contact updatedData = new Contact();
        when(userRepository.findByEmail(TEST_EMAIL))
                .thenReturn(Optional.empty());

        // Act & Assert
        UsernameNotFoundException exception = assertThrows(
                UsernameNotFoundException.class,
                () -> contactService.updateContact(TEST_CONTACT_ID, updatedData, TEST_EMAIL)
        );

        assertEquals(BAD_CREDENTIALS, exception.getMessage());
        verify(contactRepository, never()).findContactById(anyLong());
        verify(contactRepository, never()).save(any(Contact.class));
    }

    @Test
    void updateContact_WithNonExistingContact_ShouldThrowResourceNotFoundException() {
        // Arrange
        Contact updatedData = new Contact();
        when(userRepository.findByEmail(TEST_EMAIL))
                .thenReturn(Optional.of(testUser));
        when(contactRepository.findContactById(TEST_CONTACT_ID))
                .thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> contactService.updateContact(TEST_CONTACT_ID, updatedData, TEST_EMAIL)
        );

        assertTrue(exception.getMessage().contains("Contact not found"));
        assertTrue(exception.getMessage().contains(TEST_CONTACT_ID.toString()));
        verify(contactRepository, never()).save(any(Contact.class));
    }

    @Test
    void updateContact_WithContactNotBelongingToUser_ShouldThrowBadCredentialsException() {
        // Arrange
        User differentUser = new User();
        differentUser.setId(999L);

        Contact otherUsersContact = new Contact();
        otherUsersContact.setId(TEST_CONTACT_ID);
        otherUsersContact.setUser(differentUser);

        Contact updatedData = new Contact();
        updatedData.setFirstName("Updated");

        when(userRepository.findByEmail(TEST_EMAIL))
                .thenReturn(Optional.of(testUser));
        when(contactRepository.findContactById(TEST_CONTACT_ID))
                .thenReturn(Optional.of(otherUsersContact));

        // Act & Assert
        BadCredentialsException exception = assertThrows(
                BadCredentialsException.class,
                () -> contactService.updateContact(TEST_CONTACT_ID, updatedData, TEST_EMAIL)
        );

        assertEquals(BAD_CREDENTIALS, exception.getMessage());
        verify(contactRepository, never()).save(any(Contact.class));
    }

    // ==================== SearchContacts Tests ====================

    @Test
    void searchContacts_WithValidKeyword_ShouldReturnResults() {
        // Arrange
        String keyword = "John";
        List<Contact> searchResults = Arrays.asList(testContact, new Contact());

        when(userRepository.findByEmail(TEST_EMAIL))
                .thenReturn(Optional.of(testUser));
        when(contactRepository.searchContacts(TEST_USER_ID, keyword))
                .thenReturn(searchResults);

        // Act
        List<Contact> result = contactService.searchContacts(keyword, TEST_EMAIL);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(userRepository).findByEmail(TEST_EMAIL);
        verify(contactRepository).searchContacts(TEST_USER_ID, keyword);
    }

    @Test
    void searchContacts_WithNonExistingUser_ShouldThrowUsernameNotFoundException() {
        // Arrange
        String keyword = "test";
        when(userRepository.findByEmail(TEST_EMAIL))
                .thenReturn(Optional.empty());

        // Act & Assert
        UsernameNotFoundException exception = assertThrows(
                UsernameNotFoundException.class,
                () -> contactService.searchContacts(keyword, TEST_EMAIL)
        );

        assertEquals(BAD_CREDENTIALS, exception.getMessage());
        verify(contactRepository, never()).searchContacts(anyLong(), anyString());
    }

    @Test
    void searchContacts_WithNoResults_ShouldReturnEmptyList() {
        // Arrange
        String keyword = "NonExistent";
        when(userRepository.findByEmail(TEST_EMAIL))
                .thenReturn(Optional.of(testUser));
        when(contactRepository.searchContacts(TEST_USER_ID, keyword))
                .thenReturn(new ArrayList<>());

        // Act
        List<Contact> result = contactService.searchContacts(keyword, TEST_EMAIL);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(contactRepository).searchContacts(TEST_USER_ID, keyword);
    }

    @Test
    void searchContacts_WithEmptyKeyword_ShouldStillSearch() {
        // Arrange
        String keyword = "";
        when(userRepository.findByEmail(TEST_EMAIL))
                .thenReturn(Optional.of(testUser));
        when(contactRepository.searchContacts(TEST_USER_ID, keyword))
                .thenReturn(new ArrayList<>());

        // Act
        List<Contact> result = contactService.searchContacts(keyword, TEST_EMAIL);

        // Assert
        assertNotNull(result);
        verify(contactRepository).searchContacts(TEST_USER_ID, keyword);
    }

    @Test
    void searchContacts_WithNullKeyword_ShouldPassToRepository() {
        // Arrange
        when(userRepository.findByEmail(TEST_EMAIL))
                .thenReturn(Optional.of(testUser));
        when(contactRepository.searchContacts(TEST_USER_ID, null))
                .thenReturn(new ArrayList<>());

        // Act
        List<Contact> result = contactService.searchContacts(null, TEST_EMAIL);

        // Assert
        assertNotNull(result);
        verify(contactRepository).searchContacts(TEST_USER_ID, null);
    }

    @Test
    void searchContacts_WithSpecialCharacters_ShouldSearch() {
        // Arrange
        String keyword = "O'Brien";
        List<Contact> results = Arrays.asList(testContact);
        when(userRepository.findByEmail(TEST_EMAIL))
                .thenReturn(Optional.of(testUser));
        when(contactRepository.searchContacts(TEST_USER_ID, keyword))
                .thenReturn(results);

        // Act
        List<Contact> result = contactService.searchContacts(keyword, TEST_EMAIL);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(contactRepository).searchContacts(TEST_USER_ID, keyword);
    }
}
